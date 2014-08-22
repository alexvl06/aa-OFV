package co.com.alianza.domain.aggregates.autenticacion

import java.sql.Timestamp
import akka.actor.{ ActorRef, Actor, ActorLogging }
import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }
import scala.util.{ Success, Failure }
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes._
import co.com.alianza.infrastructure.dto.{ Cliente, Usuario }
import enumerations.{ TipoIdentificacion, EstadosUsuarioEnum, EstadosCliente }
import co.com.alianza.util.token.Token
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.persistence.entities.{ ReglasContrasenas, IpsUsuario }
import java.util.{ Date, Calendar }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.exceptions.{ PersistenceException, AlianzaException, TechnicalLevel }
import scala.concurrent.Future
import co.com.alianza.infrastructure.cache.UserCache

/**
 *
 */
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
                  if (valueResponse.estado == EstadosUsuarioEnum.inactivo.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoIntentosErroneos)
                  else {
                    val passwordFrontEnd = message.password
                    val passwordDB = valueResponse.contrasena.getOrElse("")

                    if (passwordFrontEnd.contentEquals(passwordDB)) {
                      //Una vez el usuario se encuentre activo en el sistema, se valida por su estado en el core de alianza.
                      val futureCliente = obtenerClienteAlianza(message.tipoIdentificacion, valueResponse.identificacion, currentSender: ActorRef)
                      realizarValidacionesCliente(futureCliente, valueResponse, message.tipoIdentificacion, message.clientIp.get, currentSender: ActorRef)
                    } else
                      currentSender ! ejecutarExcepcionPasswordInvalido(valueResponse.identificacion, valueResponse.numeroIngresosErroneos, currentSender)
                  }
                case None => currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
              }
            case zFailure(error) => currentSender ! error
          }
      }

    //Mensaje de relacion de IP a usuario en proceso de autenticacion de usuario
    case message: AgregarIPHabitualUsuario =>

      val currentSender = sender()
      val resultUsuario = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioNumeroIdentificacion(message.numeroIdentificacion);

      resultUsuario onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          value match {
            case zSuccess(response: Option[Usuario]) =>
              response match {
                case Some(valueResponse) =>
                  relacionarIpUsuarioAutenticacion(valueResponse.id.get, message.clientIp.get, message.tipoIdentificacion, message.numeroIdentificacion, valueResponse.ipUltimoIngreso.getOrElse(""), valueResponse.fechaUltimoIngreso.get, currentSender)

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
                  validarFechaContrasena(usuario.fechaCaducidad, currentSender: ActorRef)
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
      case TipoIdentificacion.CEDULA_CUIDADANIA.identificador => "N"
      case TipoIdentificacion.CEDULA_EXTRANJERIA.identificador => "N"
      case _ => "J"
    }
  }

  private def obtenerClienteAlianza(tipoIdentificacion: Int, numeroIdentificacion: String, currentSender: ActorRef): Future[Validation[PersistenceException, Option[Cliente]]] = {
    //TODO: Se debe poner el tipo de identificacion  de tipo String (tipoIdentificacion)
    val resultCliente = co.com.alianza.infrastructure.anticorruption.clientes.DataAccessAdapter.consultarCliente(ConsultaClienteRequest(tipoIdentificacion, numeroIdentificacion))
    resultCliente
  }

  private def realizarAutenticacion(numeroIdentificacion: String, nombreCliente: String, nombreCorreoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, ipActual: String, currentSender: ActorRef) = {
    currentSender ! autenticacionUsuarioValido(numeroIdentificacion, nombreCliente, nombreCorreoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, ipActual, currentSender)
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
                //TODO:Cambiar la validacion, es decir poner en el if !
                if (valueResponseCliente.wcli_estado != EstadosCliente.inactivo) {
                  //Se asocia la direccion IP a las habituales del usuario
                  val result = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.relacionarIp(idUsuario, ip)
                  //Luego de que el usuario asocia la IP, se envia a realizar autenticacion con datos a poner en el token
                  realizarAutenticacion(numeroIdentificacion, valueResponseCliente.wcli_nombre, valueResponseCliente.wcli_dir_correo, valueResponseCliente.wcli_person, ipUltimoIngreso, fechaUltimoIngreso, ip, currentSender)
                  //TODO:Se debe generar PIN de validacion de control de IP al igual que enviar correo con el mismo
                } else
                  currentSender ! ResponseMessage(Unauthorized, errorClienteInactivoSP)
              case None => currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def validarControlIpUsuario(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, currentSender: ActorRef) = {
    //Se valida que el control de direcciones IP del usuario se encuentre activo
    val resultControlIP = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerIpsUsuario(idUsuario)
    resultControlIP onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Vector[IpsUsuario]) =>
            if (response.isEmpty)
              currentSender ! ResponseMessage(Conflict, errorUsuarioControlIpInactivo)
            else
              obtenerIpHabitual(numeroIdentificacion, idUsuario, ip, nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, currentSender)
          case zFailure(error) =>
            //Cuando el usuario no posea control de direcciones IP se debe permitir autenticacion normal
            realizarAutenticacion(numeroIdentificacion, nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, ip, currentSender)
        }
    }
  }

  private def obtenerIpHabitual(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, currentSender: ActorRef) = {
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
                currentSender ! ResponseMessage(Conflict, errorUsuarioControlIpInactivo)
            }
          //En caso de que la direccion IP no sea habitual para el usuario, se procede a preguntar si desea registrarla como habitual
          //Esta excepcion debe ser mostrada en un confirm en presentacion, creando un servicio de registro de la misma
          case zFailure(error) => currentSender ! error
        }
    }
  }

  private def autenticacionUsuarioValido(numeroIdentificacion: String, nombreCliente: String, correoCliente: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimaIngreso: Date, ipActual: String, currentSender: ActorRef): String = {
    //TODO: Falta consultar si el usuario ya tiene el token relacionado, de ser asi no se genera ni se asocia, sino que se trae el token
    //El usuario paso las validaciones necesarias para  darse por autenticado
    val tokenGenerado = Token.generarToken(nombreCliente, correoCliente, tipoIdentificacion, ipUltimoIngreso, fechaUltimaIngreso)
    //Una vez se genera el token se almacena al usuario que desea realizar la auteticacion el tabla de usuarios de la aplicacion
    val resultAsociarToken = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuario(numeroIdentificacion, tokenGenerado)

    resultAsociarToken onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Int) =>
          //currentSender !  ResponseMessage(Accepted, response.toString)
          case zFailure(error) => currentSender ! error
        }
    }
    //Se establece el numero de reintentos de ingreso en cero a la aplicacion
    actualizarNumeroIngresosErroneos(numeroIdentificacion, 0, currentSender)
    //Se actualiza la fecha de ultimo ingreso y la ip de ultimo ingreso
    actualizarIpUltimoIngreso(numeroIdentificacion, ipActual, currentSender)
    actualizarFechaUltimoIngreso(numeroIdentificacion, new Timestamp((new Date).getTime()), currentSender)
    tokenGenerado
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
                  co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.actualizarEstadoUsuario(numeroIdentificacion, EstadosUsuarioEnum.inactivo.id)
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

  private def validarFechaContrasena(fechaCaducidadUsuario: Date, currentSender: ActorRef) = {

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
                  currentSender ! ResponseMessage(Unauthorized, errorUsuarioCaducidadContrasena)

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
  private val errorUsuarioControlIpInactivo = ErrorMessage("401.4", "Control IP", "El usuario no tiene activo el control de direcciones ip").toJson
  // private val errorClienteConexionCore = """{"code":"401.5","description":"No se pudo conectar con el servicio core de alianza"}"""
  //private val errorUsuarioRelacionIP = """{"code":"401.6","description":"No se pudo relacionar la direccion ip al usuario "}"""
  private val errorIntentosIngresosInvalidos = ErrorMessage("401.7", "Usuario Bloqueado", "Ha excedido el numero máximo intentos permitidos al sistema, su usuario ha sido bloqueado").toJson
  private val errorUsuarioBloqueadoIntentosErroneos = ErrorMessage("401.8", "Usuario Bloqueado", "El usuario se encuentra bloqueado").toJson
  private val errorUsuarioCaducidadContrasena = ErrorMessage("401.9", "Error Credenciales", "La contraseña del usuario ha caducado").toJson

}