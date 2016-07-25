package co.com.alianza.web

import akka.actor.ActorSelection
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import spray.routing.{ Directives, RequestContext }
import co.com.alianza.app.{ AlianzaActors, AlianzaCommons, CrossHeaders, MainActors }
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by manuel on 7/01/15.
 */
case class PermisosTransaccionalesService (kafkaActor: ActorSelection) extends Directives with AlianzaCommons with CrossHeaders {
  import PermisosTransaccionalesJsonSupport._

  val permisoTransaccionalActorSupervisor = MainActors.system.actorSelection(MainActors.permisoTransaccionalActorSupervisor.path)
  val rutaPermisosTx = "permisosTx"
  val permisosLogin = "permisosLogin"

  def route(user: UsuarioAuth) = pathPrefix(rutaPermisosTx) {
    respondWithMediaType(mediaType) {
      post {
        entity(as[GuardarPermisosAgenteMessage]) {
          permisosMessage =>
            clientIP {
              ip =>
                mapRequestContext {
                  r: RequestContext =>
                    val token = r.request.headers.find(header => header.name equals "token")
                    val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)

                    requestWithFutureAuditing[PersistenceException, GuardarPermisosAgenteMessage](r, AuditingHelper.fiduciariaTopic,
                      AuditingHelper.actualizarPermisosAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(permisosMessage))
                } {
                  requestExecute(permisosMessage.copy(idClienteAdmin = if (user.tipoCliente == clienteAdministrador) Some(user.id) else None),
                    permisoTransaccionalActorSupervisor)
                }
            }
        }
      }
    } ~ path(permisosLogin) {
      respondWithMediaType(mediaType) {
        get {
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val token = r.request.headers.find(header => header.name equals "token")
                  val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                  requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.consultaPermisosAgenteEmpresarialIndex,
                    ip.value, kafkaActor, usuario, None)
              } {
                requestExecute(ConsultarPermisosAgenteLoginMessage(user), permisoTransaccionalActorSupervisor)
              }
          }
        }
      }
    } ~ path(IntNumber) {
      idAgente =>
        respondWithMediaType(mediaType) {
          get {
            clientIP {
              ip =>
                mapRequestContext {
                  r: RequestContext =>
                    val token = r.request.headers.find(header => header.name equals "token")
                    val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                    requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic,
                      AuditingHelper.consultaPermisosAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, None)
                } {
                  requestExecute(ConsultarPermisosAgenteMessage(idAgente), permisoTransaccionalActorSupervisor)
                }
            }
          }
        }
    }
  }

}
