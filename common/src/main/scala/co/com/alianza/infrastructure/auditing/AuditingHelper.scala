package co.com.alianza.infrastructure.auditing

import akka.actor._
import co.com.alianza.infrastructure.auditing.AuditingEntities.{ AudRequest, AudResponse }
import co.com.alianza.infrastructure.auditing.AuditingMessages.AuditRequest
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import spray.http.{ HttpRequest, HttpResponse }
import spray.routing._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success
import scalaz.{ Validation, Success => zSuccess }

object AuditingHelper extends AuditingHelper {

  val fiduciariaTopic = "Fiduciaria"
  val webInmobilairiaTopic = "WebInmobiliaria"

  val cambioContrasenaIndex = "cambio-contrasena-fiduciaria"
  val autenticacionIndex = "autenticacion-fiduciaria"
  val cierreSesionIndex = "cierre-sesion-fiduciaria"
  val usuarioAgregarIpIndex = "usuario-agregar-ip-fiduciaria"
  val usuarioEliminarIpIndex = "usuario-eliminar-ip-fiduciaria"
  val usuarioConsultarIpIndex = "usuario-consultar-ip-fiduciaria"
  val actualizacionDatosUsuarioIndex = "actualizacion-datos-usuario-fiduciaria"
  val cambioContrasenaCorreoClienteIndividualIndex = "cambio-contrasena-correo-cliente-individual-fiduciaria"
  val cambioContrasenaCorreoClienteAdministradorIndex = "cambio-contrasena-correo-cliente-administrador-fiduciaria"
  val cambioContrasenaCorreoAgenteEmpresarialIndex = "cambio-contrasena-correo-agente-empresarial-fiduciaria"
  val cambioContrasenaClienteAdministradorIndex = "cambio-contrasena-cliente-administrador-fiduciaria"
  val cambioContrasenaAgenteEmpresarialIndex = "cambio-contrasena-agente-empresarial-fiduciaria"
  val cambioHorarioIndex = "cambio-horario-empresa-fiduciaria"
  val asignarContrasenaAgenteEmpresarialIndex = "asignar-contrasena-agente-empresarial-fiduciaria"
  val reiniciarContrasenaAgenteEmpresarialIndex = "reiniciar-contrasena-agente-empresarial-fiduciaria"
  val crearAgenteEmpresarialIndex = "crear-agente-empresarial-fiduciaria"
  val editarAgenteEmpresarialIndex = "editar-agente-empresarial-fiduciaria"
  val bloqueoAgenteEmpresarialIndex = "bloqueo-agente-empresarial-fiduciaria"
  val consultaPermisosAgenteEmpresarialIndex = "consulta-permisos-agente-empresarial-fiduciaria"
  val actualizarPermisosAgenteEmpresarialIndex = "actualizar-permisos-agente-empresarial-fiduciaria"
  val consultaUsuariosEmpresarialesIndex = "consulta-usuarios-empresarial-fiduciaria"
  val autoRegistroIndex = "autoregistro-fiduciaria"
  val olvidoContrasenaIndex = "olvido-contrasena-fiduciaria"
  val autovalidacionBloquearIndex = "autovalidacion-bloquear-fiduciaria"
  val autovalidacionComprobarIndex = "autovalidacion-comprobar-fiduciaria"

}

trait AuditingHelper {

  def requestWithAuiditing(ctx: RequestContext, kafkaTopic: String, elasticIndex: String, ip: String, kafkaActor: ActorSelection,
    requestParameters: Any): RequestContext = {
    ctx.withRouteResponseMapped {
      case response: HttpResponse =>

        val httpReq: HttpRequest = ctx.request

        val auditingMsg: AuditRequest =
          AuditRequest(
            AudRequest(
              httpReq.method.toString(),
              httpReq.uri.toRelative.toString(),
              requestParameters,
              ip
            ),
            AudResponse(
              response.status.intValue.toString,
              response.status.reason,
              response.entity.data.asString
            ),
            kafkaTopic,
            elasticIndex,
            httpReq.uri.toRelative.toString().split("/")(1)
          )

        kafkaActor ! auditingMsg
        response
      case a => a
    }
  }

  def requestWithFutureAuditing[E, T](ctx: RequestContext, kafkaTopic: String, elasticIndex: String, ip: String, kafkaActor: ActorSelection, futureAuditParameters: Future[Validation[E, Option[AuditingUserData]]], extraParameters: Option[T] = None)(implicit executionContext: ExecutionContext): RequestContext = {
    ctx.withRouteResponseMapped {
      case response: HttpResponse =>
        futureAuditParameters onComplete {
          case Success(validationUsuario) => {
            validationUsuario match {
              case zSuccess(usuario) => {
                val httpReq: HttpRequest = ctx.request
                val auditingMsg: AuditRequest =
                  AuditRequest(
                    AudRequest(
                      httpReq.method.toString(),
                      httpReq.uri.toRelative.toString(),
                      extraParameters.getOrElse(""),
                      ip,
                      usuario
                    ),
                    AudResponse(
                      response.status.intValue.toString,
                      response.status.reason,
                      response.entity.data.asString
                    ),
                    kafkaTopic,
                    elasticIndex,
                    httpReq.uri.toRelative.toString().split("/")(1)
                  )
                kafkaActor ! auditingMsg
              }
            }
          }
        }
        response
      case a => a
    }

  }

}
