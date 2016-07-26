package co.com.alianza.web

import akka.actor.{ ActorLogging, ActorSelection, ActorSystem }
import co.com.alianza.app.{ AlianzaCommons, CrossHeaders }
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => DataAccessAdapterAgenteEmpresarial }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => DataAccessAdapterClienteAdmin }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.token.AesUtil
import enumerations.CryptoAesParameters
import spray.routing.RequestContext
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import spray.routing.Directives

import scala.concurrent.ExecutionContext

/**
 * Created by david on 16/06/14.
 */
case class IpsUsuariosService(kafkaActor: ActorSelection, ipsUsuarioActor: ActorSelection)(implicit val ec: ExecutionContext) extends Directives with AlianzaCommons with CrossHeaders {

  import IpsUsuarioMessagesJsonSupport._
  val ipsUsuarios = "ipsUsuarios"
  val ponerIpHabitual = "ponerIpHabitual"

  def route(user: UsuarioAuth) = {

    path(ipsUsuarios) {
      get {
        respondWithMediaType(mediaType) {
          clientIP { ip =>
            mapRequestContext {
              r: RequestContext =>
                val token = r.request.headers.find(header => header.name equals "token")
                val stringToken = token match {
                  case Some(s) => s.value
                  case _ => ""
                }
                val usuario = user.tipoCliente match {
                  case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                  case TiposCliente.clienteAdministrador => DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                  case TiposCliente.agenteEmpresarial => DataAccessAdapterAgenteEmpresarial.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                }
                requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex, ip.value, kafkaActor, usuario, None)
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
                      val token = r.request.headers.find(header => header.name equals "token")
                      val stringToken = token match {
                        case Some(s) =>
                          var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
                          util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, s.value)
                        case _ => ""
                      }
                      val usuario = user.tipoCliente match {
                        case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                        case TiposCliente.clienteAdministrador => DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                        case TiposCliente.agenteEmpresarial => DataAccessAdapterAgenteEmpresarial.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                      }
                      requestWithFutureAuditing[PersistenceException, AgregarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioAgregarIpIndex, ip.value, kafkaActor, usuario, Some(agregarIpsUsuarioMessage))
                  } {
                    val agregarIpsUsuarioMessageAux: AgregarIpsUsuarioMessage = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
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
                      val usuario = user.tipoCliente match {
                        case TiposCliente.clienteIndividual => DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                        case TiposCliente.clienteAdministrador => DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                        case TiposCliente.agenteEmpresarial => DataAccessAdapterAgenteEmpresarial.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(stringToken)
                      }
                      requestWithFutureAuditing[PersistenceException, EliminarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioEliminarIpIndex, ip.value, kafkaActor, usuario, Some(eliminarIpsUsuarioMessage))
                  } {
                    val eliminarIpsUsuarioMessageAux: EliminarIpsUsuarioMessage = eliminarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
                    requestExecute(eliminarIpsUsuarioMessageAux, ipsUsuarioActor)
                  }
                }
              }
          }
        }
    }
  }
}
