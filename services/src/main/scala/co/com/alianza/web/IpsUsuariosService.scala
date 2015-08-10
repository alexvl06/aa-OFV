package co.com.alianza.web

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.routing.{RequestContext, Directives}
import co.com.alianza.infrastructure.auditing.AuditingHelper._

/**
 * Created by david on 16/06/14.
 */
class IpsUsuariosService extends Directives with AlianzaCommons with CrossHeaders {

  import IpsUsuarioMessagesJsonSupport._
  val ipsUsuarios = "ipsUsuarios"
  val ponerIpHabitual = "ponerIpHabitual"

  def route(user: UsuarioAuth) = {

    path(ipsUsuarios) {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(new ObtenerIpsUsuarioMessage(user.id), ipsUsuarioActor)
        }
      } ~
        put {
          entity(as[AgregarIpsUsuarioMessage]) {
            agregarIpsUsuarioMessage =>
              respondWithMediaType(mediaType) {
                clientIP { ip =>
                  mapRequestContext {
                    r: RequestContext =>
                      val token = r.request.headers.find(header => header.name equals "token")
                      val stringToken = token match {
                        case Some(s) => s.value
                        case _ => ""
                      }
                      val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                      requestWithFutureAuditing[PersistenceException, AgregarIpsUsuarioMessage](r, "Fiduciaria", "usuario-agregar-ip-fiduciaria", ip.value, kafkaActor, usuario, Some(agregarIpsUsuarioMessage))
                  } {
                    val agregarIpsUsuarioMessageAux: AgregarIpsUsuarioMessage = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id))
                    requestExecute(agregarIpsUsuarioMessageAux, ipsUsuarioActor)
                  }
                }
              }
          }
        } ~
        delete {
          entity(as[EliminarIpsUsuarioMessage]) {
            eliminarIpsUsuarioMessage =>
              respondWithMediaType(mediaType) {
                clientIP { ip =>
                  mapRequestContext {
                    r: RequestContext =>
                      val token = r.request.headers.find(header => header.name equals "token")
                      val stringToken = token match {
                        case Some(s) => s.value
                        case _ => ""
                      }
                      val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                      requestWithFutureAuditing[PersistenceException, EliminarIpsUsuarioMessage](r, "Fiduciaria", "usuario-eliminar-ip-fiduciaria", ip.value, kafkaActor, usuario, Some(eliminarIpsUsuarioMessage))
                  }{
                        val eliminarIpsUsuarioMessageAux: EliminarIpsUsuarioMessage = eliminarIpsUsuarioMessage.copy(idUsuario = Some(user.id))
                        requestExecute(eliminarIpsUsuarioMessageAux, ipsUsuarioActor)
                  }
                }
              }
          }
        }
    }
  }
}
