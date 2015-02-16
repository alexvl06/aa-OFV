package co.com.alianza.domain.aggregates.autenticacion

import akka.actor.{ ActorRef, Actor, ActorLogging }
import akka.actor.Props
import akka.routing.RoundRobinPool

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => confDataAdapter}
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.exceptions.{ PersistenceException, AlianzaException, TechnicalLevel }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.infrastructure.dto.{ Cliente, Usuario }
import co.com.alianza.util.token.Token
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.persistence.entities.{ ReglasContrasenas, IpsUsuario }
import enumerations.{AppendPasswordUser, TipoIdentificacion, EstadosUsuarioEnum, EstadosCliente}

import java.sql.Timestamp
import java.util.{ Date, Calendar }

import scala.concurrent.Future
import scala.util.{ Success, Failure }

import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }
import scalaz.std.AllInstances._

import spray.http.StatusCodes._

class AutenticacionActorSupervisor extends Actor with ActorLogging {
  import akka.actor.SupervisorStrategy._
  import akka.actor.OneForOneStrategy

  val autenticacionActor = context.actorOf(Props[AutenticacionActor].withRouter(RoundRobinPool(nrOfInstances = 5)), "autenticacionActor")

  def receive = {

    case message: Any =>
      autenticacionActor forward message

  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class AutenticacionActor extends Actor with ActorLogging {

  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {

    //Mensaje de autenticacion de usuario
    case message: AutenticarMessage =>

      val currentSender = sender()
      val result = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioNumeroIdentificacion(message.numeroIdentificacion)
      result onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          value match {
            case zSuccess(response: Option[Usuario]) =>
              response match {
                case Some(valueResponse) =>
                  if (valueResponse.estado == EstadosUsuarioEnum.bloqueContraseña.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoIntentosErroneos)
                  else if(valueResponse.estado == EstadosUsuarioEnum.pendienteActivacion.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteActivacion)
                  else if(valueResponse.estado == EstadosUsuarioEnum.pendienteConfronta.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteConfronta)
                  else if(valueResponse.estado == EstadosUsuarioEnum.pendienteReinicio.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteReinicio)
                  else {
                    //Se pone un "pase" para que no sea tan facil hacer unHashSha512 de los password planos
                    val passwordFrontEnd = Crypto.hashSha512( message.password.concat( AppendPasswordUser.appendUsuariosFiducia ) )
                    val passwordDB = valueResponse.contrasena.getOrElse("")
                    //Crypto.hashSha512(message.contrasena))
                    if (passwordFrontEnd.contentEquals(passwordDB)) {
                      //Una vez el usuario se encuentre activo en el sistema, se valida por su estado en el core de alianza.
                      val futureCliente = obtenerClienteAlianza(message.tipoIdentificacion, valueResponse.identificacion, currentSender: ActorRef)
                      realizarValidacionesCliente(futureCliente, valueResponse, message.tipoIdentificacion, message.clientIp.get, currentSender: ActorRef)
                    } else
                      currentSender ! ejecutarExcepcionPasswordInvalido(valueResponse.identificacion, valueResponse.numeroIngresosErroneos, currentSender)
                  }
                case None =>
                  currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
              }
            case zFailure(error) => currentSender ! error
          }
      }

    //Mensaje de relacion de IP a usuario en proceso de autenticacion de usuario
    case message: AgregarIPHabitualUsuario =>

      val currentSender = sender()
      val resultUsuario = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioId(message.idUsuario.get)

      resultUsuario onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          value match {
            case zSuccess(response: Option[Usuario]) =>
              response match {
                case Some(valueResponse) =>
                    relacionarIpUsuarioAutenticacion(valueResponse.id.get, message.clientIp.get, valueResponse.tipoIdentificacion, valueResponse.identificacion, valueResponse.ipUltimoIngreso.getOrElse(""), valueResponse.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), currentSender)
                case None => currentSender ! ResponseMessage(Unauthorized, "Error al obtener usuario por numero de identificacion")
              }
            case zFailure(error) => currentSender ! error
          }
      }
  }

  private def realizarValidacionesCliente(futureCliente: Future[Validation[PersistenceException, Option[Cliente]]], usuario: Usuario, messageTipoIdentificacion: Int, ip: String, currentSender: ActorRef) {
    futureCliente onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[Cliente]) =>
            response match {
              case Some(valueResponseCliente) =>
                if (getTipoPersona(messageTipoIdentificacion) != valueResponseCliente.wcli_person)
                  currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
                else if (valueResponseCliente.wcli_estado != EstadosCliente.inactivo) {
                  //Se valida la caducidad de la contraseña
                  validarFechaContrasena(usuario.id.get, usuario.fechaCaducidad, currentSender: ActorRef)
                  //Validacion de control de direccion IP del usuario
                  validarControlIpUsuario(usuario.identificacion, usuario.id.get, ip, valueResponseCliente.wcli_nombre, valueResponseCliente.wcli_dir_correo, valueResponseCliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), currentSender: ActorRef)
                } else
                  currentSender ! ResponseMessage(Unauthorized, errorClienteInactivoSP)
              case None => currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  //Se valida la naturalidad de la persona que realiza la autenticaciónS
  private def getTipoPersona(idTipoIdent: Int): String = {
    idTipoIdent match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case _ => "N"
    }
  }

  private def obtenerClienteAlianza(tipoIdentificacion: Int, numeroIdentificacion: String, currentSender: ActorRef): Future[Validation[PersistenceException, Option[Cliente]]] = {
    //TODO: Se debe poner el tipo de identificacion  de tipo String (tipoIdentificacion)
    val resultCliente = co.com.alianza.infrastructure.anticorruption.clientes.DataAccessAdapter.consultarCliente(ConsultaClienteRequest(tipoIdentificacion, numeroIdentificacion))
    resultCliente
  }

  private def realizarAutenticacion(numeroIdentificacion: String, nombreCliente: String, nombreCorreoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, ipActual: String, currentSender: ActorRef) = {
    autenticacionUsuarioValido(numeroIdentificacion, nombreCliente, nombreCorreoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, ipActual, currentSender)
  }

  private def relacionarIpUsuarioAutenticacion(idUsuario: Int, ip: String, tipoIdentificacion: Int, numeroIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, currentSender: ActorRef) = {
    //Se asocia IP como valida para el usuario, ya se ha hecho validacion de estado del cliente, pero se vuelve a validar.
    val futureCliente = obtenerClienteAlianza(tipoIdentificacion, numeroIdentificacion, currentSender: ActorRef)
    futureCliente onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[Cliente]) =>
            response match {
              case Some(valueResponseCliente) =>
                if (valueResponseCliente.wcli_estado != EstadosCliente.inactivo) {
                  //Se asocia la direccion IP a las habituales del usuario
                  val result = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.relacionarIp(idUsuario, ip)
                  currentSender ! "Registro de IP Exitoso"
                } else currentSender ! ResponseMessage(Unauthorized, errorClienteInactivoSP)
              case None => currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def validarControlIpUsuario(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, currentSender: ActorRef) = {
    //Se valida que el control de direcciones IP del usuario se encuentre activo
    val resultControlIP = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerIpsUsuario(idUsuario)

    val tokenGenerado: String = Token.generarToken(nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso)
    val resultAsociarToken: Future[Validation[PersistenceException, Int]] = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuario(numeroIdentificacion, tokenGenerado)

    //Se establece el numero de reintentos de ingreso en cero a la aplicacion
    actualizarNumeroIngresosErroneos(numeroIdentificacion, 0, currentSender)
    //Se actualiza la fecha de ultimo ingreso y la ip de ultimo ingreso
    actualizarIpUltimoIngreso(numeroIdentificacion, ip, currentSender)
    actualizarFechaUltimoIngreso(numeroIdentificacion, new Timestamp((new Date).getTime()), currentSender)

    resultControlIP onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>

            if (response.isEmpty) {
              resultAsociarToken onComplete {
                case Failure(failure) => currentSender ! failure
                case Success(value) =>

                  for {
                    expiracionSesion <- ValidationT(confDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave))
                  } yield {
                    MainActors.sesionActorSupervisor ! CrearSesionUsuario(tokenGenerado, expiracionSesion)
                    currentSender ! ResponseMessage(Conflict, ErrorMessage("401.4", "Control IP", "El usuario no tiene activo el control de direcciones ip", tokenGenerado).toJson)
                  }

              }
            }
            else obtenerIpHabitual(numeroIdentificacion, idUsuario, ip, nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, currentSender, tokenGenerado)

          case zFailure(error) =>
            currentSender ! error
        }
    }
  }

  private def obtenerIpHabitual(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, currentSender: ActorRef, tokenGenerado: String) = {
    //En caso de que este activo, se valida que la ip de acceso es una direccion habitual registrada
    val resultIpValida = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerIpUsuarioValida(idUsuario, ip)
    resultIpValida onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[IpsUsuario]) =>
            response match {
              case Some(valueResponse) =>
                realizarAutenticacion(numeroIdentificacion, nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, ip, currentSender)
              case None =>
                for {
                  asociar <- ValidationT(co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuario(numeroIdentificacion, tokenGenerado))
                  expiracionSesion <- ValidationT(confDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave))
                } yield {
                  MainActors.sesionActorSupervisor ! CrearSesionUsuario(tokenGenerado, expiracionSesion)
                  currentSender ! ResponseMessage(Conflict, ErrorMessage("401.4", "Control IP", "El usuario no tiene activo el control de direcciones ip", tokenGenerado).toJson)
                }
            }
          //En caso de que la direccion IP no sea habitual para el usuario, se procede a preguntar si desea registrarla como habitual
          //Esta excepcion debe ser mostrada en un confirm en presentacion, creando un servicio de registro de la misma
          case zFailure(error) => currentSender ! error
        }
    }
  }



  private def autenticacionUsuarioValido(numeroIdentificacion: String, nombreCliente: String, correoCliente: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimaIngreso: Date, ipActual: String, currentSender: ActorRef){
    //TODO: Falta consultar si el usuario ya tiene el token relacionado, de ser asi no se genera ni se asocia, sino que se trae el token
    //El usuario paso las validaciones necesarias para  darse por autenticado
    val tokenGenerado = Token.generarToken(nombreCliente, correoCliente, tipoIdentificacion, ipUltimoIngreso, fechaUltimaIngreso)
    //Una vez se genera el token se almacena al usuario que desea realizar la auteticacion el tabla de usuarios de la aplicacion
    val resultAsociarToken = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuario(numeroIdentificacion, tokenGenerado)

    for {
      asociar <- ValidationT(resultAsociarToken)
      expiracionSesion <- ValidationT(confDataAdapter.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_SESION.llave))
    } yield {
      MainActors.sesionActorSupervisor ! CrearSesionUsuario(tokenGenerado, expiracionSesion)
      currentSender ! tokenGenerado
    }
    
  }

  private def actualizarNumeroIngresosErroneos(numeroIdentificacion: String, numeroIngresosErroneos: Int, currentSender: ActorRef) = {
    val resultActualizarReintentosCero = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarNumeroIngresosErroneos(numeroIdentificacion, numeroIngresosErroneos)
    resultActualizarReintentosCero onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) =>
          //currentSender !  ResponseMessage(Accepted, response.toString)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def actualizarIpUltimoIngreso(numeroIdentificacion: String, ipActual: String, currentSender: ActorRef) = {
    val resultActualizarIpUltimoIngreso = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarIpUltimoIngreso(numeroIdentificacion, ipActual)
    resultActualizarIpUltimoIngreso onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) =>
          //currentSender !  ResponseMessage(Accepted, response.toString)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def actualizarFechaUltimoIngreso(numeroIdentificacion: String, fechaActual: Timestamp, currentSender: ActorRef) = {
    val resultActualizarFechaUltimoIngreso = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarFechaUltimoIngreso(numeroIdentificacion, fechaActual)
    resultActualizarFechaUltimoIngreso onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) =>
          //currentSender !  ResponseMessage(Accepted, response.toString)
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def ejecutarExcepcionPasswordInvalido(numeroIdentificacion: String, numeroIngresosErroneos: Int, currentSender: ActorRef) = {
    actualizarNumeroIngresosErroneos(numeroIdentificacion, numeroIngresosErroneos + 1, currentSender)
    val resultLlave = co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter.obtenerRegla("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA")
    resultLlave onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[ReglasContrasenas]) =>
            response match {
              case Some(valueResponse) =>
                if (valueResponse.valor.toInt == numeroIngresosErroneos + 1) {
                  co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarEstadoUsuario(numeroIdentificacion, EstadosUsuarioEnum.bloqueContraseña.id)
                  currentSender ! ResponseMessage(Unauthorized, errorIntentosIngresosInvalidos)
                } else {
                  currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
                }

              case None =>
                currentSender ! AlianzaException(new Exception("Error al obtener clave de intentos erroneos al sistema"), TechnicalLevel, "Error al obtener clave de intentos erroneos al sistema")
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def validarFechaContrasena(idUsuario: Int, fechaCaducidadUsuario: Date, currentSender: ActorRef) = {

    val calendarFechaCaducidad = Calendar.getInstance()
    calendarFechaCaducidad.setTime(fechaCaducidadUsuario)
    val calendarFechaActual = Calendar.getInstance()

    val resultLlave = co.com.alianza.infrastructure.anticorruption.contrasenas.DataAccessAdapter.obtenerRegla("DIAS_VALIDA")
    resultLlave onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[ReglasContrasenas]) =>
            response match {
              case Some(valueResponse) =>
                calendarFechaCaducidad.add(Calendar.DATE, valueResponse.valor.toInt)
                if (calendarFechaActual.compareTo(calendarFechaCaducidad) >= 0)
                {
                  val tokenGenerado: String = Token.generarTokenCaducidadContrasena(idUsuario)
                  val resp: String = ErrorMessage("401.9", "Error Credenciales", "La contraseña del usuario ha caducado", tokenGenerado).toJson
                  currentSender ! ResponseMessage(Unauthorized, resp)
                }

              case None =>
                currentSender ! AlianzaException(new Exception("Error Obteniendo Clave de Días validos de contraseña al sistema"), TechnicalLevel, "Error al obtener clave de dias validos de contraseña al sistema")

            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private val errorClienteInactivoSP = ErrorMessage("401.1", "Error Cliente Alianza", "Cliente inactivo en core de alianza").toJson
  private val errorClienteNoExisteSP = ErrorMessage("401.2", "Error Cliente Alianza", "No existe el cliente en el core de alianza").toJson
  private val errorUsuarioCredencialesInvalidas = ErrorMessage("401.3", "Error Credenciales", "Credenciales invalidas para acceder al portal de alianza fiduciaria").toJson
  // private val errorClienteConexionCore = """{"code":"401.5","description":"No se pudo conectar con el servicio core de alianza"}"""
  //private val errorUsuarioRelacionIP = """{"code":"401.6","description":"No se pudo relacionar la direccion ip al usuario "}"""
  private val errorIntentosIngresosInvalidos = ErrorMessage("401.7", "Usuario Bloqueado", "Ha excedido el numero máximo intentos permitidos al sistema, su usuario ha sido bloqueado").toJson
  private val errorUsuarioBloqueadoIntentosErroneos = ErrorMessage("401.8", "Usuario Bloqueado", "El usuario se encuentra bloqueado").toJson
  //private val errorUsuarioCaducidadContrasena = ErrorMessage("401.9", "Error Credenciales", "La contraseña del usuario ha caducado").toJson
  private val errorUsuarioBloqueadoPendienteActivacion = ErrorMessage("401.10", "Usuario Bloqueado", "El usuario se encuentra pendiente de activación").toJson
  private val errorUsuarioBloqueadoPendienteConfronta = ErrorMessage("401.11", "Usuario Bloqueado", "El usuario se encuentra bloqueado pendiente preguntas de seguridad").toJson
  private val errorUsuarioBloqueadoPendienteReinicio = ErrorMessage("401.12", "Usuario Bloqueado", "El usuario se encuentra bloqueado pendiente de reiniciar contraseña").toJson
}