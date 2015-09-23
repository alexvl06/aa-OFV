package co.com.alianza.web

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.OlvidoContrasenaMessage
import co.com.alianza.infrastructure.messages.UsuarioMessage
import spray.routing.{RequestContext, Directives}
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser



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

                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autoRegistroIndex, ip.value, kafkaActor, usuario.copy( contrasena = null))) {
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
                    val nuevoUsuario = desbloqueoMsg.toDesbloquarMessage.copy(clientIp = Some(ip.value))
                    requestExecute(nuevoUsuario, usuariosActor)
                }
              }
          }
        }
      } ~ path("olvidoContrasena"){
            post {
              //Reinicio de contrasena de la cuenta alianza fiduciaria (Implica cambio en el estado del usuario)
              clientIP { ip =>
                entity(as[OlvidoContrasenaMessage]) {
                  olvidarContrasena =>
                    mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.olvidoContrasenaIndex, ip.value, kafkaActor, olvidarContrasena)) {
                      requestExecute(olvidarContrasena, usuariosActor)
                    }
                }
              }
          }
      }
    }
  }

}
