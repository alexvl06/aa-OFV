package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => DataAccessAdapterAgenteEmpresarial }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => DataAccessAdapterClienteAdmin }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes
import spray.routing.{ Directives, RequestContext }

import scala.concurrent.Future
import scalaz.Validation

/**
 * Created by david on 16/06/14.
 */
case class IpsUsuariosService(kafkaActor: ActorSelection, ipsUsuarioActor: ActorSelection)(implicit val system: ActorSystem) extends Directives
    with AlianzaCommons with CrossHeaders {

  import IpsUsuarioMessagesJsonSupport._
  import system.dispatcher

  val ipsUsuarios = "ipsUsuarios"

  def route(user: UsuarioAuth) = {
    path(ipsUsuarios) {
      if (user.tipoCliente.eq(TiposCliente.comercialSAC))
        complete((StatusCodes.Unauthorized, "Tipo usuario SAC no esta autorizado para gestionar las ip's"))
      else
        get {
          respondWithMediaType(mediaType) {
            clientIP { ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario = obtenerUsuario(r, user)
                  requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
                    ip.value, kafkaActor, usuario, None)
              } {
                requestExecute(new ObtenerIpsUsuarioMessage(user.id, user.tipoCliente), ipsUsuarioActor)
              }
            }
          }
        } ~
          put {
            entity(as[AgregarIpsUsuarioMessage]) {
              agregarIpsUsuarioMessage =>
                respondWithMediaType(mediaType) {
                  clientIP { ip =>
                    mapRequestContext {
                      r: RequestContext =>
                        val usuario = obtenerUsuario(r, user)
                        requestWithFutureAuditing[PersistenceException, AgregarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic,
                          AuditingHelper.usuarioAgregarIpIndex, ip.value, kafkaActor, usuario, Some(agregarIpsUsuarioMessage))
                    } {
                      val agregarIpsUsuarioMessageAux = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
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
                        val usuario = obtenerUsuario(r, user)
                        requestWithFutureAuditing[PersistenceException, EliminarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic,
                          AuditingHelper.usuarioEliminarIpIndex, ip.value, kafkaActor, usuario, Some(eliminarIpsUsuarioMessage))
                    } {
                      val eliminarIpsUsuarioMessageAux = eliminarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
                      requestExecute(eliminarIpsUsuarioMessageAux, ipsUsuarioActor)
                    }
                  }
                }
            }
          }
    }
  }

  private def obtenerUsuario(r: RequestContext, user: UsuarioAuth): Future[Validation[PersistenceException, Option[AuditingUserData]]] = {
    val token = r.request.headers.find(header => header.name equals "token")
    val stringToken = token match {
      case Some(s) => s.value
      case _ => ""
    }

    user.tipoCliente match {
      case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
      case TiposCliente.clienteAdministrador => DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
      case TiposCliente.agenteEmpresarial => DataAccessAdapterAgenteEmpresarial.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
    }
  }
}
