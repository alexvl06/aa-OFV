package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes
import spray.routing.{ Directives, RequestContext }

/**
 * Created by manuel on 7/01/15.
 */
case class PermisosTransaccionalesService(kafkaActor: ActorSelection, permisoTransaccionalActor: ActorSelection)(implicit val system: ActorSystem)
    extends Directives with AlianzaCommons with CrossHeaders {

  import PermisosTransaccionalesJsonSupport._
  import system.dispatcher

  val rutaPermisosTx = "permisosTx"
  val permisosLogin = "permisosLogin"
  val permisoFideicomiso = "permisoFideicomiso"

  def route(user: UsuarioAuth) = pathPrefix(rutaPermisosTx) {
    respondWithMediaType(mediaType) {
      post {
        //TODO: esta validacion no va acá !!
        if (user.tipoCliente.eq(TiposCliente.comercialSAC))
          complete((StatusCodes.Unauthorized, "Tipo usuario SAC no está autorizado para realizar esta acción"))
        else
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
                    requestExecute(
                      permisosMessage.copy(idClienteAdmin = if (user.tipoCliente == clienteAdministrador) Some(user.id) else None),
                      permisoTransaccionalActor
                    )
                    requestExecute(
                      permisosMessage.copy(idClienteAdmin = if (user.tipoCliente == clienteAdministrador) Some(user.id) else None),
                      permisoTransaccionalActor
                    )
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
                requestExecute(ConsultarPermisosAgenteLoginMessage(user), permisoTransaccionalActor)
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
                  requestExecute(ConsultarPermisosAgenteMessage(idAgente), permisoTransaccionalActor)
                }
            }
          }
        }
    } ~ path(permisoFideicomiso) {
      respondWithMediaType(mediaType) {
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
                  requestExecute(ConsultarFideicomiso(user), permisoTransaccionalActor)
                }
            }
          }
        }
      }
    }
  }

}
