package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.{ CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.messages.{IpsUsuarioMessagesJsonSupport, AutenticacionMessagesJsonSupport, AutenticarMessage, AgregarIPHabitualUsuario}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

class AutenticacionService extends Directives with AlianzaCommons with CrossHeaders {

  import AutenticacionMessagesJsonSupport._

  def route = {
    path("autenticar") {
      post {
        entity(as[AutenticarMessage]) {
          autenticacion =>
            respondWithMediaType(mediaType) {
              //clientIP { ip =>
                //val nuevaAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                requestExecute(autenticacion, autenticacionActor)
              //}
            }
        }
      }
    }
  }

  def routeAutenticado( user: UsuarioAuth ) = {
    path("ponerIpHabitual") {
      post {
        entity(as[AgregarIPHabitualUsuario]) {
          ponerIpHabitual =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                val nuevoPonerIpHabitual: AgregarIPHabitualUsuario = ponerIpHabitual.copy(clientIp = Some(ip.value), idUsuario =  Some(user.id) )
                requestExecute(nuevoPonerIpHabitual, autenticacionActor)
              }
            }
        }
      }
    }
  }
}
