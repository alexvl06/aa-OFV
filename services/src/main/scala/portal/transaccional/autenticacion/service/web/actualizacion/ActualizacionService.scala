package portal.transaccional.autenticacion.service.web.actualizacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ ValidacionException, PersistenceException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.util.json.JsonUtil
import portal.transaccional.autenticacion.service.drivers.actualizacion.ActualizacionRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ StandardRoute, RequestContext }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by hernando on 10/10/16.
 */
case class ActualizacionService(user: UsuarioAuth, kafkaActor: ActorSelection,
  actualizacionRepo: ActualizacionRepository)(implicit val ec: ExecutionContext)
    extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val datos = "datos"
  val paises = "paises"
  val ciudades = "ciudades"
  val comprobar = "comprobar"
  val tiposCorreo = "tiposCorreo"
  val ocupaciones = "ocupaciones"
  val actualizacion = "actualizacion"
  val envioCorrespondencia = "envioCorrespondencia"
  val actividadesEconomicas = "actividadesEconomicas"

  def route = {
    pathPrefix(actualizacion) {
      get {
        obtenerPaises ~ obtenerCiudades ~ obtenerTiposCorreo ~
          obtenerEnvioCorrespondencia ~ obtenerOcupaciones ~
          obtenerActividades ~ obtenerDatos ~ comprobarDatos
      } ~ put {
        actualizar
      }
    }
  }

  def actualizar = {
    clientIP {
      ip =>
        entity(as[ActualizacionMessage]) {
          actualizacion =>
            mapRequestContext {
              r: RequestContext =>
                val usuario: Option[AuditingUserData] = getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario)
                requestAuditing[PersistenceException, ActualizacionMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.actualizacionDatosUsuarioIndex,
                  ip.value, kafkaActor, usuario, Option(actualizacion))
            } {
              val resultado: Future[String] = actualizacionRepo.actualizarDatos(user, actualizacion)
              onComplete(resultado) {
                case Success(value)if(value.equals("OK")) => complete(value)
                case Success(value) => manejarError(value)
                case Failure(ex) => manejarError(ex)
              }
            }
        }
    }
  }

  def obtenerPaises = {
    pathPrefix(paises) {
      pathEndOrSingleSlash {
        val result: Future[Seq[Pais]] = actualizacionRepo.obtenerPaises()
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, value))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def obtenerCiudades = {
    pathPrefix(ciudades / IntNumber) {
      (pais: Int) =>
        pathEndOrSingleSlash {
          val result: Future[Seq[Ciudad]] = actualizacionRepo.obtenerCiudades(pais)
          onComplete(result) {
            case Success(value) => complete((StatusCodes.OK, value))
            case Failure(ex) => manejarError(ex)
          }
        }
    }
  }

  def obtenerTiposCorreo = {
    pathPrefix(tiposCorreo) {
      pathEndOrSingleSlash {
        val result: Future[Seq[TipoCorreo]] = actualizacionRepo.obtenerTiposCorreo()
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, value))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def obtenerEnvioCorrespondencia = {
    pathPrefix(envioCorrespondencia) {
      pathEndOrSingleSlash {
        val result: Future[Seq[EnvioCorrespondencia]] = actualizacionRepo.obtenerEnviosCorrespondencia()
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, value))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def obtenerOcupaciones = {
    pathPrefix(ocupaciones) {
      pathEndOrSingleSlash {
        val result: Future[Seq[Ocupacion]] = actualizacionRepo.obtenerOcupaciones()
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, value))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def obtenerActividades = {
    pathPrefix(actividadesEconomicas) {
      pathEndOrSingleSlash {
        val result: Future[Seq[ActividadEconomica]] = actualizacionRepo.obtenerActividadesEconomicas()
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, value))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def comprobarDatos = {
    pathPrefix(comprobar) {
      pathEndOrSingleSlash {
        val result: Future[Boolean] = actualizacionRepo.comprobarDatos(user)
        onComplete(result) {
          case Success(value) if (value) => complete((StatusCodes.Conflict, value.toString()))
          case Success(value) if (!value) => complete((StatusCodes.OK, value.toString()))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  def obtenerDatos = {
    pathPrefix(datos) {
      pathEndOrSingleSlash {
        val result: Future[DatosCliente] = actualizacionRepo.obtenerDatos(user)
        onComplete(result) {
          case Success(value) => complete((StatusCodes.OK, JsonUtil.toJson(value)))
          case Failure(ex) => manejarError(ex)
        }
      }
    }
  }

  private def manejarError(error: Throwable): StandardRoute = {
    error match {
      case ex: ValidacionException =>
        ex.printStackTrace(); complete((StatusCodes.Conflict, ex))
      case ex: Throwable =>
        ex.printStackTrace(); complete((StatusCodes.InternalServerError, "Error inesperado"))
      case any: Any => println("any"); complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
