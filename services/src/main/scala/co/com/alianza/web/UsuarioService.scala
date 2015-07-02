package co.com.alianza.web


import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import spray.routing.{RequestContext, Directives}
import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.OlvidoContrasenaMessage
import scala.Some
import co.com.alianza.infrastructure.messages.UsuarioMessage


/**
 *
 * @author seven4n
 */
class UsuarioService  extends Directives with AlianzaCommons   with CrossHeaders{

  import UsuariosMessagesJsonSupport._

  def route = {
     pathPrefix("autoregistro") {
      path("usuario" ) {
        put {
          entity(as[UsuarioMessage]) {
           usuario =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, "Fiduciaria", "autoregistro-fiduciaria", ip.value, kafkaActor, usuario.copy( contrasena = Crypto.hashSha512(usuario.contrasena.concat(AppendPasswordUser.appendUsuariosFiducia))))) {
                  val nuevoUsuario: UsuarioMessage = usuario.copy(clientIp = Some(ip.value))
                  requestExecute(nuevoUsuario, usuariosActor)
                }
              }
            }
          }
      }
     } ~ path("desbloquear") {
        post {
          entity(as[DesbloquarWebMessage]) {
            desbloqueoMsg =>
              respondWithMediaType(mediaType) {
                clientIP {
                  ip =>
                    val nuevoUsuario: DesbloquarMessage = desbloqueoMsg.toDesbloquarMessage.copy(clientIp = Some(ip.value))
                    requestExecute(nuevoUsuario, usuariosActor)
                }
              }
          }
        }
      } ~ path("olvidoContrasena"){
          //pathEndOrSingleSlash {
            post {
              //Reinicio de contrasena de la cuenta alianza fiduciaria (Implica cambio en el estado del usuario)
              entity(as[OlvidoContrasenaMessage]) {
                olvidarContrasena =>
                  requestExecute(olvidarContrasena, usuariosActor)
            }
          }
        //}
      }
    }
  }

}
