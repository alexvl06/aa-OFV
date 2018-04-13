package portal.transaccional.autenticacion.service.web.autenticacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper.requestWithAuiditing
import co.com.alianza.util.token.AesUtil
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialRepository, AutenticacionEmpresaRepository, AutenticacionRepository, AutenticacionUsuarioRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class AutenticacionService(
  autenticacionRepositorio: AutenticacionRepository,
  autenticacionEmpresaRepositorio: AutenticacionEmpresaRepository,
  autenticacionComercialRepositorio: AutenticacionComercialRepository,
  /**OFV LOGIN FASE 1**/
  autenticacionUsuarioRepository: AutenticacionUsuarioRepository,
  kafkaActor: ActorSelection
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val autenticar = "autenticar"
  val autenticarUsuarioEmpresa = "autenticarUsuarioEmpresa"
  val comercialPath = "comercial"
  /**OFV LOGIN FASE 1**/
  var autenticarGeneral = "autenticacion-general"

  val route: Route = {
    pathPrefix(autenticar) {
      pathEndOrSingleSlash {
        autenticarUsuarioIndividual
      }
    } ~
      pathPrefix(autenticarUsuarioEmpresa) {
        pathEndOrSingleSlash {
          autenticarUsuarioEmpresarial
        }
      } ~
      pathPrefix(comercialPath / autenticar) {
        pathEndOrSingleSlash {
          autenticarUsuarioComercial
        }
        /**OFV LOGIN FASE 1**/
      } ~
      pathPrefix(autenticarGeneral) {
        pathEndOrSingleSlash {
          autenticacionGeneral
        }
      }
  }

  private def autenticarUsuarioIndividual = {
    post {
      entity(as[AutenticarRequest]) {
        request =>
          clientIP { ip =>
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
              AuditingHelper.autenticacionIndex, ip.value, kafkaActor, request.copy(password = null))) {
              val resultado = autenticacionRepositorio.autenticar(request.tipoIdentificacion, request.numeroIdentificacion, request.password, ip.value)

              onComplete(resultado) {
                case Success(token) => encriptarToken(token)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  private def autenticarUsuarioEmpresarial = {
    post {
      entity(as[AutenticarUsuarioEmpresarialRequest]) {
        request =>
          clientIP { ip =>
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
              AuditingHelper.autenticacionIndex, ip.value, kafkaActor, request.copy(password = null))) {
              val resultado: Future[String] = autenticacionEmpresaRepositorio.autenticarUsuarioEmpresa(request.nit, request.usuario, request.password, ip.value)

              onComplete(resultado) {
                case Success(token) => encriptarToken(token)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  private def autenticarUsuarioComercial = {
    post {
      entity(as[AutenticarUsuarioComercialRequest]) {
        request =>
          clientIP { ip =>
            val tipoCliente: String = TiposCliente.getTipoCliente(request.tipoUsuario).toString
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
              AuditingHelper.autenticacionComercialIndex, ip.value, kafkaActor, request.copy(contrasena = null, tipoCliente = Option(tipoCliente)))) {
              val resultado: Future[String] = autenticacionComercialRepositorio.autenticar(
                request.usuario.toLowerCase,
                request.tipoUsuario, request.contrasena, ip.value
              )
              onComplete(resultado) {
                case Success(token) => encriptarToken(token)
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  /**OFV LOGIN FASE 1**/
  private def autenticacionGeneral = {
    post {
      entity(as[UsuarioGenRequest]) {
        request =>
          clientIP { ip =>
            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
              AuditingHelper.autenticacionIndex, ip.value, kafkaActor, request.copy())) {
              val resultado = autenticacionUsuarioRepository.autenticarGeneral(request, ip.value)
              onComplete(resultado) {
                case Success(token) => complete((StatusCodes.OK, "{ \"token\" : \"" + token + "\"}"))
                case Failure(ex) => execution(ex)
              }
            }
          }
      }
    }
  }

  def encriptarToken(token: String): StandardRoute = {
    complete((StatusCodes.OK, AesUtil.encriptarToken(token)))
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException =>
        complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable =>
        ex.printStackTrace()
        complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
