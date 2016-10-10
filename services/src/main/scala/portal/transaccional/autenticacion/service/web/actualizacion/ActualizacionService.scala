package portal.transaccional.autenticacion.service.web.actualizacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.exceptions.{ ValidacionException, PersistenceException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
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
case class ActualizacionService(user: UsuarioAuth, kafkaActor: ActorSelection, actualizacionRepo: ActualizacionRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

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
        obtenerDatos
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
              val resultado = actualizacionRepo.actualizarDatos()
              onComplete(resultado) {
                case Success(value) => complete(value.toString)
                case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para obtener ip"))
              }
            }
        }
    }
  }

  def obtenerDatos = {
    pathPrefix(paises) {
      val result: Future[Seq[Pais]] = actualizacionRepo.obtenerPaises()
      execution(result)
    } ~
      pathPrefix(ciudades / IntNumber) {
        (pais: Int) =>
          val result: Future[Seq[Ciudad]] = actualizacionRepo.obtenerCiudades()
          execution(result)
      } ~
      pathPrefix(tiposCorreo) {
        val result: Future[Seq[TipoCorreo]] = actualizacionRepo.obtenerTiposCorreo()
        execution(result)
      } ~
      pathPrefix(envioCorrespondencia) {
        val result: Future[Seq[EnvioCorrespondencia]] = actualizacionRepo.obtenerEnviosCorrespondencia()
        execution(result)
      } ~
      pathPrefix(ocupaciones) {
        val result: Future[Seq[Ocupacion]] = actualizacionRepo.obtenerOcupaciones()
        execution(result)
      } ~
      pathPrefix(actividadesEconomicas) {
        val result: Future[Seq[ActividadEconomica]] = actualizacionRepo.obtenerActividadesEconomicas()
        execution(result)
      } ~
      pathPrefix(datos) {
        val result: Future[DatosCliente] = actualizacionRepo.obtenerDatos()
        execution(result)
      } ~
      pathPrefix(comprobar) {
        val result: Future[Boolean] = actualizacionRepo.comprobarDatos()
        execution(result)
      }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      //TODO: AGREGAR MARSHABLE
      case value: Seq[Pais] => complete((StatusCodes.OK, value.toString()))
      case value: Seq[Ciudad] => complete((StatusCodes.OK, value.toString()))
      case value: Seq[TipoCorreo] => complete((StatusCodes.OK, value.toString()))
      case value: Seq[EnvioCorrespondencia] => complete((StatusCodes.OK, value.toString()))
      case value: Seq[Ocupacion] => complete((StatusCodes.OK, value.toString()))
      case value: Seq[ActividadEconomica] => complete((StatusCodes.OK, value.toString()))
      case value: DatosCliente => complete((StatusCodes.OK, value.toString()))
      case value: Boolean if (value) => complete((StatusCodes.OK, value.toString()))
      case value: Boolean if (!value) => complete((StatusCodes.OK, value.toString()))

      case ex: ValidacionException => complete((StatusCodes.Conflict, ex))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case _ => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
