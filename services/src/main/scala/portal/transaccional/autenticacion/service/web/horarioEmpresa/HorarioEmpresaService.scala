package portal.transaccional.autenticacion.service.web.horarioEmpresa

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
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
case class HorarioEmpresaService(user: UsuarioAuth, kafkaActor: ActorSelection,
  horarioEmpresaRepository: HorarioEmpresaRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

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
          clientIP {
            ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                  requestAuditing[PersistenceException, AgregarHorarioEmpresaRequest](r, AuditingHelper.fiduciariaTopic,
                    AuditingHelper.cambioHorarioIndex, ip.value, kafkaActor, usuario, Some(request))
              } {
                val resultado: Future[Int] = horarioEmpresaRepository.agregar(user, request.diaHabil, request.sabado, request.horaInicio, request.horaFin)
                onComplete(resultado) {
                  case Success(value) => complete(value.toString)
                  case Failure(ex) => execution(ex)
                }
              }
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

}