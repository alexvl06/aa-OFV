package co.com.alianza.domain.aggregates.autenticacion

import akka.actor.{ ActorRef, Actor, ActorLogging }

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessAdapter => confDataAdapter}
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.exceptions.{ PersistenceException, AlianzaException, TechnicalLevel }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.infrastructure.dto.{ Cliente, Usuario, UsuarioEmpresarial }
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

/**
 * Created by manuel on 10/12/14.
 */
class AutenticacionUsuarioEmpresaActor extends AutenticacionActor {

  override def receive = {
   //Mensaje de autenticación de usuario cliente de empresa
    case message: AutenticarUsuarioEmpresaMessage =>

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
                      val futureCliente = obtenerClienteAlianza(valueResponse.tipoIdentificacion, valueResponse.identificacion, currentSender: ActorRef)
                      realizarValidacionesClienteEmpresa(futureCliente, valueResponse, valueResponse.tipoIdentificacion, message.clientIp.get, currentSender: ActorRef)
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

  private def realizarValidacionesClienteEmpresa(futureCliente: Future[Validation[PersistenceException, Option[Cliente]]], usuario: UsuarioEmpresarial, messageTipoIdentificacion: Int, ip: String, currentSender: ActorRef) {
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
                  validarControlIpUsuario(usuario.identificacion, usuario.id, ip, valueResponseCliente.wcli_nombre, valueResponseCliente.wcli_dir_correo, valueResponseCliente.wcli_person, usuario.ipUltimoIngreso.getOrElse(""), usuario.fechaUltimoIngreso.getOrElse(new Date(System.currentTimeMillis())), currentSender: ActorRef)
                } else
                  currentSender ! ResponseMessage(Unauthorized, errorClienteInactivoSP)
              case None => currentSender ! ResponseMessage(Unauthorized, errorClienteNoExisteSP)
            }
          case zFailure(error) => currentSender ! error
        }
    }
  }

}
