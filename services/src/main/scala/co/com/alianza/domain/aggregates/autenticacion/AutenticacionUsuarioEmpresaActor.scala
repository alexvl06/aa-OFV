package co.com.alianza.domain.aggregates.autenticacion

import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.commons.enumerations.TiposCliente
import scala.concurrent.duration._

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => confDataAdapter}
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.exceptions.{PersistenceException, AlianzaException, TechnicalLevel}
import co.com.alianza.util.clave.Crypto
import co.com.alianza.infrastructure.dto._
import co.com.alianza.util.token.Token
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.persistence.entities.{ReglasContrasenas, IpsUsuario}
import enumerations.{AppendPasswordUser, TipoIdentificacion, EstadosUsuarioEnum, EstadosCliente}

import java.sql.Timestamp
import java.util.{Date, Calendar}

import scala.concurrent._
import scala.util.{Success, Failure}

import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._

import spray.http.StatusCodes._

/**
 * Created by manuel on 10/12/14.
 */
class AutenticacionUsuarioEmpresaActor extends AutenticacionActor {

  import co.com.alianza.util.json.MarshallableImplicits._

  implicit val timeout: Timeout = Timeout(10 seconds)

  override def receive = {

    case message: AutenticarUsuarioEmpresarialMessage =>
      val currentSender = sender
      MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialMessage(usuario = Some(message.usuario), nit = Some(message.nit)) onComplete {
        case Success(Some(u)) =>
          self tell(AutenticarUsuarioEmpresarialAgenteMessage(message.tipoIdentificacion, message.numeroIdentificacion, message.nit, message.usuario, message.password, message.clientIp), currentSender)
        case Success(None) =>
          MainActors.usuariosActorSupervisor ? ConsultaUsuarioEmpresarialAdminMessage(usuario = Some(message.usuario), nit = Some(message.nit)) onComplete {
            case Success(Some(u)) =>
              self tell(AutenticarUsuarioEmpresarialAdminMessage(message.tipoIdentificacion, message.numeroIdentificacion, message.nit, message.usuario, message.password, message.clientIp), currentSender)
            case Success(None) =>
              currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
            case Failure(t) =>
              currentSender ! t
          }
        case Failure(t) =>
          currentSender ! t
      }

    //Mensaje de autenticación de usuario cliente de empresa
    case message: AutenticarUsuarioEmpresarialAgenteMessage =>
      val currentSender = sender()
      val result = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialPorNitYUsuario(message.nit, message.usuario)
      result onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          value match {
            case zSuccess(response: Option[UsuarioEmpresarial]) =>
              response match {
                case Some(valueResponse) =>
                  if (valueResponse.estado == EstadosUsuarioEnum.bloqueContraseña.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoIntentosErroneos)
                  else if (valueResponse.estado == EstadosUsuarioEnum.pendienteActivacion.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteActivacion)
                  //else if(valueResponse.estado == EstadosUsuarioEnum.pendienteConfronta.id)
                  //currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteConfronta)
                  else if (valueResponse.estado == EstadosUsuarioEnum.pendienteReinicio.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteReinicio)
                  else {
                    //Se pone un "pase" para que no sea tan facil hacer unHashSha512 de los password planos
                    val passwordFrontEnd = Crypto.hashSha512(message.password.concat(AppendPasswordUser.appendUsuariosFiducia))
                    val passwordDB = valueResponse.contrasena.getOrElse("")
                    //Crypto.hashSha512(message.contrasena))
                    if (passwordFrontEnd.contentEquals(passwordDB)) {
                      validarFechaContrasena(valueResponse.id, valueResponse.fechaCaducidad, currentSender: ActorRef)
                      validarControlIpUsuarioAgenteEmpresarial(valueResponse.identificacion, valueResponse.id, message.clientIp.get, valueResponse.nombreUsuario.get, valueResponse.correo, getTipoPersona(valueResponse.tipoIdentificacion), valueResponse.ipUltimoIngreso.getOrElse(""), valueResponse.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), message.nit, currentSender: ActorRef)
                    } else
                      currentSender ! ejecutarExcepcionPasswordInvalido(valueResponse.identificacion, valueResponse.numeroIngresosErroneos, currentSender)
                  }
                case None =>
                  currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
              }
            case zFailure(error) => currentSender ! error
          }
      }

    case message: AutenticarUsuarioEmpresarialAdminMessage =>
      val currentSender = sender()
      val result = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialAdminPorNitYUsuario(message.nit, message.usuario)
      result onComplete {
        case Failure(failure) => currentSender ! failure
        case Success(value) =>
          value match {
            case zSuccess(response: Option[UsuarioEmpresarialAdmin]) =>
              response match {
                case Some(valueResponse) =>
                  if (valueResponse.estado == EstadosUsuarioEnum.bloqueContraseña.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoIntentosErroneos)
                  else if (valueResponse.estado == EstadosUsuarioEnum.pendienteActivacion.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteActivacion)
                  //else if(valueResponse.estado == EstadosUsuarioEnum.pendienteConfronta.id)
                  //currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteConfronta)
                  else if (valueResponse.estado == EstadosUsuarioEnum.pendienteReinicio.id)
                    currentSender ! ResponseMessage(Unauthorized, errorUsuarioBloqueadoPendienteReinicio)
                  else {
                    //Se pone un "pase" para que no sea tan facil hacer unHashSha512 de los password planos
                    val passwordFrontEnd = Crypto.hashSha512(message.password.concat(AppendPasswordUser.appendUsuariosFiducia))
                    val passwordDB = valueResponse.contrasena.getOrElse("")
                    //Crypto.hashSha512(message.contrasena))
                    if (passwordFrontEnd.contentEquals(passwordDB)) {
                      //Una vez el usuario se encuentre activo en el sistema, se valida por su estado en el core de alianza.
                      val futureCliente = obtenerClienteAlianza(valueResponse.tipoIdentificacion, valueResponse.identificacion, currentSender: ActorRef)
                      realizarValidacionesUsuarioEmpresarialAdmin(futureCliente, valueResponse, valueResponse.tipoIdentificacion, message.clientIp.get, message.nit, currentSender: ActorRef)
                    } else
                      currentSender ! ejecutarExcepcionPasswordInvalido(valueResponse.identificacion, valueResponse.numeroIngresosErroneos, currentSender)
                  }
                case None =>
                  currentSender ! ResponseMessage(Unauthorized, errorUsuarioCredencialesInvalidas)
              }
            case zFailure(error) => currentSender ! error
          }
      }

  }

  private def realizarValidacionesUsuarioEmpresarialAdmin(futureCliente: Future[Validation[PersistenceException, Option[Cliente]]], usuario: UsuarioEmpresarialAdmin, messageTipoIdentificacion: Int, ip: String, nit: String, currentSender: ActorRef) {
    futureCliente onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response: Option[Cliente]) =>
            response match {
              case Some(valueResponseCliente) =>
                if (getTipoPersona(messageTipoIdentificacion) != valueResponseCliente.wcli_person)
                  currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
                else if (valueResponseCliente.wcli_estado != EstadosCliente.bloqueoContraseña) {
                  //Se valida la caducidad de la contraseña
                  validarFechaContrasena(usuario.id, usuario.fechaCaducidad, currentSender: ActorRef)
                  //Validacion de control de direccion IP del usuario
                  //TODO:                  validarEmpresaCliente(usuario.id.get, )
                  validarControlIpUsuarioEmpresarialAdmin(usuario.identificacion, usuario.id, ip, valueResponseCliente.wcli_nombre, valueResponseCliente.wcli_dir_correo, valueResponseCliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), nit, currentSender: ActorRef)
                } else
                  currentSender ! ResponseMessage(Unauthorized, errorClienteInactivoSP)
              case None => currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

  protected def validarControlIpUsuarioEmpresarialAdmin(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, nit: String, currentSender: ActorRef) = {
    //Se valida que el control de direcciones IP del usuario se encuentre activo
    val resultControlIP = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerIpsUsuarioEmpresarialAdmin(idUsuario)

    val tokenGenerado: String = Token.generarToken(nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, TiposCliente.clienteAdministrador, Some(nit))
    val resultAsociarToken: Future[Validation[PersistenceException, Int]] = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuarioEmpresarialAdmin(idUsuario, tokenGenerado)

    // TODO: para usuario empresarial Admin
    //actualizarNumeroIngresosErroneos(numeroIdentificacion, 0, currentSender)
    //actualizarFechaUltimoIngreso(numeroIdentificacion, new Timestamp((new Date).getTime()), currentSender)

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

  protected def validarControlIpUsuarioAgenteEmpresarial(numeroIdentificacion: String, idUsuario: Int, ip: String, nombreCliente: String, correoUsuario: String, tipoIdentificacion: String, ipUltimoIngreso: String, fechaUltimoIngreso: Date, nit: String, currentSender: ActorRef) = {
    //Se valida que el control de direcciones IP del usuario se encuentre activo
    val resultControlIP = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerIpsUsuarioEmpresarial(idUsuario)

    val tokenGenerado: String = Token.generarToken(nombreCliente, correoUsuario, tipoIdentificacion, ipUltimoIngreso, fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(nit))
    val resultAsociarToken: Future[Validation[PersistenceException, Int]] = co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.asociarTokenUsuarioEmpresarial(idUsuario, tokenGenerado)

    // TODO: para usuario agente empresarial
    //actualizarNumeroIngresosErroneos(numeroIdentificacion, 0, currentSender)
    //actualizarFechaUltimoIngreso(numeroIdentificacion, new Timestamp((new Date).getTime()), currentSender)

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


}
