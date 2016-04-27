package co.com.alianza.web.empresa

import spray.routing.Directives
import co.com.alianza.app.{ AlianzaActors, MainActors, CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.messages.empresa._
import akka.actor.ActorSystem
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

class OtpService extends Directives with AlianzaCommons with CrossHeaders with AlianzaActors {

  private val OTPPath = "OTP"
  private val registrarDispositivo = "registrarDispositivo"
  private val removerDispositivo = "removerDispositivo"
  private val habilitarDispositivo = "habilitarDispositivo"
  private val deshabilitarDispositivo = "deshabilitarDispositivo"

  import CrearAgenteEMessageJsonSupport._

  def secureUserRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix(OTPPath) {
      pathPrefix(registrarDispositivo) {
        pathEndOrSingleSlash {
          respondWithMediaType(mediaType) {
            get {
              parameters('correo.?, 'usuario.?, 'nombre.?, 'estado.?) { (correo, usuario, nombre, estado) =>
                //Lista de todos los usuarios
                requestExecute(GetAgentesEmpresarialesMessage(correo.getOrElse(null), usuario.getOrElse(null), nombre.getOrElse(null), estado.get.toInt, user.id), agenteEmpresarialActor)
              }
            }
          }
        }

      }
    }

  }
}
