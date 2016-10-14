package portal.transaccional.autenticacion.service.web.horarioEmpresa

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.horarioEmpresa.HorarioEmpresaRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * @author s4n on 16/06/15.
 */
case class HorarioEmpresaService(user: UsuarioAuth, kafkaActor: ActorSelection, horarioEmpresaRepository: HorarioEmpresaRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val diaFestivoPath = "diaFestivo"
  val horarioEmpresaPath = "horarioEmpresa"
  val validarHorarioPath = "validarHorario"

  val route: Route = {
    path(horarioEmpresaPath) {
      obtenerHorarioEmpresa ~ agregarHorarioEmpresa
    } ~ path(diaFestivoPath) {
      esDiaFestivo
    } ~ path(validarHorarioPath) {
      validarHorario
    }
  }

  private def obtenerHorarioEmpresa = {
    get {
      val resultado: Future[Option[ResponseObtenerHorario]] = horarioEmpresaRepository.obtener(user.identificacion)
      onComplete(resultado) {
        case Success(value) => complete(value)
        case Failure(ex) => execution(ex)
      }
    }
  }

  private def agregarHorarioEmpresa = {
    put {
      entity(as[AgregarHorarioEmpresaRequest]) {
        request =>
          // TODO: AUDITORIA by:Jonathan
          val resultado: Future[Int] = horarioEmpresaRepository.agregar(user, request.diaHabil, request.sabado, request.horaInicio, request.horaFin)
          onComplete(resultado) {
            case Success(value) => complete(value.toString)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  private def esDiaFestivo = {
    post {
      entity(as[DiaFestivoRequest]) {
        request =>
          val resultado: Future[Boolean] = horarioEmpresaRepository.esDiaFestivo(request.fecha)
          onComplete(resultado) {
            case Success(value) => complete(value.toString)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  private def validarHorario = {
    get {
      parameters('idUsuarioRecurso.as[Option[String]], 'tipoIdentificacion.as[Option[Int]]) {
        (idUsuarioRecurso, tipoIdentificacion) =>
          val resultado: Future[Boolean] = horarioEmpresaRepository.validar(user, idUsuarioRecurso, tipoIdentificacion)
          onComplete(resultado) {
            case Success(value) => complete(value.toString)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  def execution(ex: Throwable): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

  /*
  //TODO: agregar la auditoria
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
    }
  }*/

}