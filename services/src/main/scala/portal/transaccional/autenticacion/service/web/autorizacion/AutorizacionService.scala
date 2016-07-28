package portal.transaccional.autenticacion.service.web.autorizacion

import akka.actor.{ ActorSelection }
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.autorizacion.AutorizacionUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.usuario.{ UsuarioEmpresarialAdminRepository, UsuarioEmpresarialRepository, UsuarioRepository }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ Route, StandardRoute }

import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by jonathan on 27/07/16.
 */
case class AutorizacionService(usuarioRepository: UsuarioRepository, usuarioAgenteRepository: UsuarioEmpresarialRepository,
    usuarioAdminRepository: UsuarioEmpresarialAdminRepository, autorizacionRepository: AutorizacionUsuarioRepository,
    kafkaActor: ActorSelection)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val invalidarTokenPath = "invalidarToken"
  val validarTokenPath = "validarToken"

  val route: Route = {
    path(invalidarTokenPath) {
      pathEndOrSingleSlash {
        invalidarToken
      }
    } ~ path(validarTokenPath / Segment) {
      token =>
        validarToken(token)
    }
  }

  private def invalidarToken = {
    entity(as[InvalidarTokenRequest]) {
      token =>
        delete {
          clientIP { ip =>
            val util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
            val decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token.token)
            val tipoCliente = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

            // TODO: Matar sesion
            val resultado: Future[Int] = if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
              usuarioAgenteRepository.invalidarToken(token.token)
            } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
              usuarioAdminRepository.invalidarToken(token.token)
            } else {
              usuarioRepository.invalidarToken(token.token)
            }

            // TODO: Auditoria
            /*mapRequestContext((r: RequestContext) => requestWithFutureAuditing[PersistenceException, Usuario]
              (r, AuditingHelper.fiduciariaTopic, AuditingHelper.cierreSesionIndex, ip.value, kafkaActor, usuario)) {*/
            onComplete(resultado) {
              case Success(value) => complete(value.toString)
              case Failure(ex) => execution(ex)
            }
            //}

          }
        }
    }
  }

  private def validarToken(token: String) = {
    get {
      parameters('url, 'ipRemota) {
        (url, ipRemota) =>
          val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
          val resultado: Future[Boolean] = if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
            autorizacionRepository.autorizarUrl(token, url)
          } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
            //TODO: aqui va el empresarial
            autorizacionRepository.autorizarUrl(token, url)
          } else {
            //TODO: aqui va el empresarial
            autorizacionRepository.autorizarUrl(token, url)
          }
          // TODO: Auditoria
          onComplete(resultado) {
            case Success(value) =>
              complete(value.toString)
            case Failure(ex) => ex.printStackTrace(); execution(ex)
          }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => complete((StatusCodes.Forbidden, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
