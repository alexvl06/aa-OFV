package co.com.alianza.web

import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import co.com.alianza.commons.enumerations.TiposCliente
import spray.routing.{RequestContext, Directives}
import co.com.alianza.app.{ CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

class AutenticacionService extends Directives with AlianzaCommons with CrossHeaders {

  import AutenticacionMessagesJsonSupport._


  def route = {
    path("autenticar") {
      post {
        entity(as[AutenticarMessage]) {
          autenticacion =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex, ip.value, kafkaActor, autenticacion.copy( password = null, clientIp = Some(ip.value)))) {
                  val nuevaAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                  requestExecute(nuevaAutenticacion, autenticacionActor)
		            }
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
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex, ip.value, kafkaActor, autenticacion.copy( password = null, clientIp = Some(ip.value)))) {
                  val nuevaAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                  requestExecute(nuevaAutenticacion, autenticacionUsuarioEmpresaActor)
                }
              }
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

                if (user.tipoCliente.id == TiposCliente.clienteAdministrador.id) {
                  requestExecute(AgregarIPHabitualUsuarioEmpresarialAdmin(Some(user.id), Some(ip.value)), autenticacionUsuarioEmpresaActor)
                }

                else if (user.tipoCliente.id == TiposCliente.agenteEmpresarial.id) {
                  requestExecute(AgregarIPHabitualUsuarioEmpresarialAgente(Some(user.id), Some(ip.value)), autenticacionUsuarioEmpresaActor)
                }

                else {
                  val nuevoPonerIpHabitual: AgregarIPHabitualUsuario = ponerIpHabitual.copy(clientIp = Some(ip.value), idUsuario =  Some(user.id) )
                  requestExecute(nuevoPonerIpHabitual, autenticacionActor)
                }

              }
            }
        }
      }
    } ~ path("actualizarInactividad") {
      post {
        complete {
          "ok"
        }
      }
    }
  }
}