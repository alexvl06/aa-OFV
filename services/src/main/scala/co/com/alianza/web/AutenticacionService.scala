package co.com.alianza.web

import spray.routing.Directives
import co.com.alianza.app.{ CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.messages._

class AutenticacionService extends Directives with AlianzaCommons with CrossHeaders {

  import AutenticacionMessagesJsonSupport._

  def route = {
    path("autenticar") {
      post {
        entity(as[AutenticarMessage]) {
          autenticacion =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                val nuevaAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                requestExecute(nuevaAutenticacion, autenticacionActor)
              }
            }
        }
      }
    } ~ path("autenticarUsuarioEmpresa") {
      post {
        entity(as[AutenticarUsuarioEmpresarialMessage]) {
          autenticacion =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                val nuevaAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                requestExecute(nuevaAutenticacion, autenticacionUsuarioEmpresaActor)
              }
            }
        }
      }
    } ~ path("ponerIpHabitual") {
      post {
        entity(as[AgregarIPHabitualUsuario]) {
          ponerIpHabitual =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                val nuevoPonerIpHabitual: AgregarIPHabitualUsuario = ponerIpHabitual.copy(clientIp = Some(ip.value))
                requestExecute(nuevoPonerIpHabitual, autenticacionActor)
              }
            }
        }
      }
    }
  }
}