package co.com.alianza.web

import akka.actor.{ActorSelection, ActorSystem}
import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa._
import spray.http.StatusCodes
import spray.routing.{Directives, RequestContext}

import scala.concurrent.ExecutionContext

/**
 * @author hernando on 16/06/15.
 */
case class HorarioEmpresaService(kafkaActor: ActorSelection, horarioEmpresaActor: ActorSelection)(implicit val system: ActorSystem) extends Directives with AlianzaCommons with CrossHeaders {

  import HorarioEmpresaJsonSupport._
  import system.dispatcher
  val diaFestivo = "diaFestivo"
  val horarioEmpresa = "horarioEmpresa"
  val validarHorario = "validarHorario"

  def route(user: UsuarioAuth) = {

    path(horarioEmpresa) {
      get {
        respondWithMediaType(mediaType) {
          requestExecute(ObtenerHorarioEmpresaMessage(user.id, user.tipoCliente), horarioEmpresaActor)
        }
      } ~
        put {
          if (user.tipoCliente.eq(TiposCliente.comercialSAC))
            complete((StatusCodes.Unauthorized, "Tipo usuario SAC no está autorizado para realizar esta acción"))
          else
          entity(as[AgregarHorarioEmpresaMessage]) {
            agregarHorarioEmpresaMessage =>
              respondWithMediaType(mediaType) {
                clientIP {
                  ip =>
                    mapRequestContext {
                      r: RequestContext =>
                        val token = r.request.headers.find(header => header.name equals "token")
                        val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)

                        requestWithFutureAuditing[PersistenceException, AgregarHorarioEmpresaMessage](r, AuditingHelper.fiduciariaTopic,
                          AuditingHelper.cambioHorarioIndex, ip.value, kafkaActor, usuario, Some(agregarHorarioEmpresaMessage))
                    } {
                      requestExecute(agregarHorarioEmpresaMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id)), horarioEmpresaActor)
                    }
                }

              }
          }
        }
    } ~
      path(diaFestivo) {
        post {
          entity(as[DiaFestivoMessage]) {
            diaFestivoMessage =>
              respondWithMediaType(mediaType) {
                requestExecute(diaFestivoMessage, horarioEmpresaActor)
              }
          }
        }
      } ~
      path(validarHorario) {
        get {
          parameters('idUsuarioRecurso.as[Option[String]], 'tipoIdentificacion.as[Option[Int]]) {
            (idUsuarioRecurso, tipoIdentificacion) =>
              respondWithMediaType(mediaType) {
                requestExecute(ValidarHorarioEmpresaMessage(user, idUsuarioRecurso, tipoIdentificacion), horarioEmpresaActor)
              }
          }
        }
      }
  }

}