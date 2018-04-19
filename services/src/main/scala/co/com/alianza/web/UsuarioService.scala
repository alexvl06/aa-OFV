package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.OlvidoContrasenaMessage
import co.com.alianza.infrastructure.messages.UsuarioMessage
import spray.routing.{ Directives, RequestContext }
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.AppendPasswordUser
import portal.transaccional.autenticacion.service.web.autorizacion.AuditityUser
import spray.http.StatusCodes

import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 * @author seven4n
 */
case class UsuarioService(kafkaActor: ActorSelection, usuariosActor: ActorSelection)(implicit val system: ActorSystem) extends Directives with AlianzaCommons
    with CrossHeaders {

  import UsuariosMessagesJsonSupport._

  //tipos clientes
  val agente = TiposCliente.agenteEmpresarial.toString
  val comercialSAC = TiposCliente.comercialSAC.toString
  val admin = TiposCliente.clienteAdministrador.toString
  val individual = TiposCliente.clienteIndividual.toString
  val comercialAdmin = TiposCliente.comercialAdmin.toString
  val comercialValores = TiposCliente.comercialValores.toString
  val comercialFiduciaria = TiposCliente.comercialFiduciaria.toString
  val adminInmobiliaria = TiposCliente.clienteAdminInmobiliario.toString
  val agenteInmobiliario = TiposCliente.agenteInmobiliario.toString
  val agenteInmobiliarioInterno = TiposCliente.agenteInmobiliarioInterno.toString

  def route = {
    pathPrefix("autoregistro") {
      path("usuario") {
        put {
          entity(as[UsuarioMessage]) {
            usuario =>
              respondWithMediaType(mediaType) {
                clientIP { ip =>
                  mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.autoRegistroIndex, ip.value, kafkaActor, usuario.copy(contrasena = null))) {
                    val nuevoUsuario: UsuarioMessage = usuario.copy(clientIp = Some(ip.value))
                    requestExecute(nuevoUsuario, usuariosActor)
                  }
                }
              }
          }
        }
      } ~ path("desbloquear") {
        post {
          entity(as[DesbloquearMessage]) {
            desbloqueoMsg =>
              respondWithMediaType(mediaType) {
                requestExecute(desbloqueoMsg, usuariosActor)
              }
          }
        }
      } ~ path("olvidoContrasena") {
        post {
          //Reinicio de contrasena de la cuenta alianza fiduciaria (Implica cambio en el estado del usuario)
          clientIP { ip =>
            entity(as[OlvidoContrasenaMessage]) {
              olvidarContrasena =>
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                  AuditingHelper.olvidoContrasenaIndex, ip.value, kafkaActor, olvidarContrasena)) {
                  requestExecute(olvidarContrasena, usuariosActor)
                }
            }
          }
        }
      } ~ path("enviar-habeasdata") {
        post {
          clientIP { ipRemota =>
            {
              headerValueByName("token") { token =>
                entity(as[UsuarioAceptaHabeasDataMessage]) {
                  usuarioHabeasData =>
                    val decriptedToken: String = AesUtil.desencriptarToken(token)
                    val usuario: AuditityUser = getTokenData(decriptedToken)
                    if (usuario.tipoCliente == agenteInmobiliario || usuario.tipoCliente == agenteInmobiliarioInterno || usuario.tipoCliente == adminInmobiliaria)
                      requestExecute(usuarioHabeasData, usuariosActor)
                    else
                      complete(StatusCodes.Unauthorized)
                }
              }
            }
          }
        }
      }
    }
  }

  private def getTokenData(token: String): AuditityUser = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    val tipoCliente = nToken.getCustomClaim("tipoCliente").toString
    //TODO: el nit no lo pide si es tipo comercial
    val nit = if (tipoCliente == agente || tipoCliente == admin || tipoCliente == adminInmobiliaria || tipoCliente == agenteInmobiliario || tipoCliente == agenteInmobiliarioInterno) nToken.getCustomClaim("nit").toString else ""
    val lastIp = nToken.getCustomClaim("ultimaIpIngreso").toString
    val user = nToken.getCustomClaim("nombreUsuario").toString
    val email = nToken.getCustomClaim("correo").toString
    val lastEntry = nToken.getCustomClaim("ultimaFechaIngreso").toString
    val nitType = nToken.getCustomClaim("tipoIdentificacion").toString
    AuditityUser(email, nit, nitType, user, lastIp, lastEntry, tipoCliente)
  }

}
