package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.commons.enumerations.TiposCliente
import spray.routing.{ Directives, RequestContext }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

import scala.concurrent.ExecutionContext

case class AutenticacionService(kafkaActor: ActorSelection, autenticacionActor: ActorSelection, autenticacionUsuarioEmpresaActor: ActorSelection) extends Directives with AlianzaCommons with CrossHeaders {

  import AutenticacionMessagesJsonSupport._
  import system.dispatcher
  def route = {
    path("autenticar") {
      post {
        entity(as[AutenticarMessage]) {
          autenticacion =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex, ip.value, kafkaActor, autenticacion.copy(password = null, clientIp = Some(ip.value)))) {
                  val mensajeAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                  requestExecute(mensajeAutenticacion, autenticacionActor)
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
                mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.autenticacionIndex, ip.value, kafkaActor, autenticacion.copy(password = null, clientIp = Some(ip.value)))) {
                  val mensajeAutenticacion = autenticacion.copy(clientIp = Some(ip.value))
                  requestExecute(mensajeAutenticacion, autenticacionUsuarioEmpresaActor)
                }
              }
            }
        }
      }
    }
  }

  def routeAutenticado(user: UsuarioAuth) = {
    path("ponerIpHabitual") {
      post {
        entity(as[AgregarIPHabitualUsuario]) {
          ponerIpHabitual =>
            respondWithMediaType(mediaType) {
              clientIP { ip =>

                if (user.tipoCliente.id == TiposCliente.clienteAdministrador.id) {
                  requestExecute(AgregarIPHabitualUsuarioEmpresarialAdmin(Some(user.id), Some(ip.value)), autenticacionUsuarioEmpresaActor)
                } else if (user.tipoCliente.id == TiposCliente.agenteEmpresarial.id) {
                  requestExecute(AgregarIPHabitualUsuarioEmpresarialAgente(Some(user.id), Some(ip.value)), autenticacionUsuarioEmpresaActor)
                } else {
                  val nuevoPonerIpHabitual: AgregarIPHabitualUsuario = ponerIpHabitual.copy(clientIp = Some(ip.value), idUsuario = Some(user.id))
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