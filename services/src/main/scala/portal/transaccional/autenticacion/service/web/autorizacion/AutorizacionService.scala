package portal.transaccional.autenticacion.service.web.autorizacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.autorizacion.{ AutorizacionUsuarioEmpresarialAdminRepository, AutorizacionUsuarioEmpresarialRepository, AutorizacionUsuarioRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioEmpresarialAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing.{ RequestContext, Route, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by jonathan on 27/07/16.
 */
case class AutorizacionService(usuarioRepository: UsuarioRepository, usuarioAgenteRepository: UsuarioEmpresarialRepository,
  usuarioAdminRepository: UsuarioEmpresarialAdminRepository, autorizacionRepository: AutorizacionUsuarioRepository, kafkaActor: ActorSelection,
  autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository, autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val invalidarTokenPath = "invalidarToken"
  val validarTokenPath = "validarToken"

  val agente = TiposCliente.agenteEmpresarial.toString
  val admin = TiposCliente.clienteAdministrador.toString
  val individual = TiposCliente.clienteIndividual.toString

  val route: Route = {
    path(invalidarTokenPath) {
      pathEndOrSingleSlash {
        invalidarToken
      }
    } ~ path(validarTokenPath / Segment) {
      token =>  validarToken(token)
    }
  }

  private def invalidarToken = {
    entity(as[InvalidarTokenRequest]) {
      token =>
        delete {
          clientIP { ip =>

            val decryptedToken = AesUtil.desencriptarToken(token.token, "AutorizacionService.invalidarToken")

            val usuario = getTokenData(decryptedToken)
            val resultado = usuario.tipoCliente match {
              case `agente` => autorizacionAgenteRepo.invalidarToken(token.token)
              case `admin` => autorizacionAdminRepo.invalidarToken(token.token)
              case _ => autorizacionRepository.invalidarToken(token.token)
            }

            mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.cierreSesionIndex, ip.value,
              kafkaActor, usuario)) {
              onComplete(resultado) {
                case Success(value) => complete(value.toString)
                case Failure(ex) => execution(ex)
              }
            }
          }
        }
    }
  }

  private def validarToken(token: String) = {
    get {
      parameters('url, 'ipRemota) {
        (url, ipRemota) =>

          val usuario = getTokenData(token)

          val resultado = usuario.tipoCliente match {
            case `agente` => autorizacionAgenteRepo.autorizar(token, url, ipRemota)
            case `admin` => autorizacionAdminRepo.autorizar(token, url, ipRemota)
            case _ => autorizacionRepository.autorizarUrl(token, url)
          }

          onComplete(resultado) {
            case Success(value) => execution(value)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case value: Autorizado => complete((StatusCodes.OK, value.usuario))
      case value: Prohibido => complete((StatusCodes.Forbidden, value.usuario))
      case ex: NoAutorizado => complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

  private def getTokenData(token: String): AuditityUser = {

    val nToken = Token.getToken(token).getJWTClaimsSet
    val tipoCliente = nToken.getCustomClaim("tipoCliente").toString
    val nit = if (tipoCliente == individual) "" else nToken.getCustomClaim("nit").toString
    val lastIp = nToken.getCustomClaim("ultimaIpIngreso").toString
    val user = nToken.getCustomClaim("nombreUsuario").toString
    val email = nToken.getCustomClaim("correo").toString
    val lastEntry = nToken.getCustomClaim("ultimaFechaIngreso").toString
    val nitType = nToken.getCustomClaim("tipoIdentificacion").toString

    AuditityUser(email, nit, nitType, user, lastIp, lastEntry, tipoCliente)
  }

}
