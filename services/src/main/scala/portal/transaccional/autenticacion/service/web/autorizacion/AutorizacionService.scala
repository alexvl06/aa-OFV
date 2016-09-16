package portal.transaccional.autenticacion.service.web.autorizacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.autorizacion._
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
 * Created by s4n 2016
 */
case class AutorizacionService(
    usuarioRepository: UsuarioRepository,
    usuarioAgenteRepository: UsuarioEmpresarialRepository,
    usuarioAdminRepository: UsuarioEmpresarialAdminRepository,
    autorizacionRepository: AutorizacionUsuarioRepository,
    kafkaActor: ActorSelection,
    autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository,
    autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository,
    autorizacionComercialRepo: AutorizacionUsuarioComercialRepository,
    autorizacionComercialAdminRepo: AutorizacionUsuarioComercialAdminRepository
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val invalidarTokenPath = "invalidarToken"
  val validarTokenPath = "validarToken"

  //tipos clientes
  val agente = TiposCliente.agenteEmpresarial.toString
  val comercialSAC = TiposCliente.comercialSAC.toString
  val admin = TiposCliente.clienteAdministrador.toString
  val individual = TiposCliente.clienteIndividual.toString
  val comercialAdmin = TiposCliente.comercialAdmin.toString
  val comercialValores = TiposCliente.comercialValores.toString
  val comercialFiduciaria = TiposCliente.comercialFiduciaria.toString

  val route: Route = {
    path(invalidarTokenPath) {
      pathEndOrSingleSlash {
        invalidarToken
      }
    } ~ path(validarTokenPath / Segment) {
      token => validarToken(token)
    }
  }

  private def invalidarToken = {
    entity(as[InvalidarTokenRequest]) {
      tokenRequest =>
        delete {
          clientIP { ip =>
            val encriptedToken: String = tokenRequest.token
            val token: String = AesUtil.desencriptarToken(encriptedToken)
            val usuario = getTokenData(token)
            val resultado: Future[Int] = usuario.tipoCliente match {
              case `admin` => autorizacionAdminRepo.invalidarToken(token, encriptedToken)
              case `agente` => autorizacionAgenteRepo.invalidarToken(token, encriptedToken)
              case `individual` => autorizacionRepository.invalidarToken(token, encriptedToken)
              case `comercialSAC` => autorizacionComercialRepo.invalidarTokenSAC(token, encriptedToken)
              case `comercialAdmin` => autorizacionComercialAdminRepo.invalidarToken(token, encriptedToken)
              case `comercialFiduciaria` | `comercialValores` => autorizacionComercialRepo.invalidarToken(token, encriptedToken)
              case _ => Future.failed(NoAutorizado("Tipo usuario no existe"))
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
          val encriptedToken = AesUtil.encriptarToken(token)
          val resultado = usuario.tipoCliente match {
            case `agente` => autorizacionAgenteRepo.autorizar(token, encriptedToken, url, ipRemota)
            case `admin` => autorizacionAdminRepo.autorizar(token, encriptedToken, url, ipRemota)
            case `individual` => autorizacionRepository.autorizar(token, encriptedToken, url)
            //TODO: Agregar la autorizacion de url para los tipo comerciales (Pendiente HU) By : Hernando
            case `comercialFiduciaria` => obtenerUsuarioComercialMock(TiposCliente.comercialFiduciaria, usuario.usuario)
            case `comercialValores` => obtenerUsuarioComercialMock(TiposCliente.comercialValores, usuario.usuario)
            case `comercialAdmin` => obtenerUsuarioComercialMock(TiposCliente.clienteAdministrador, usuario.usuario)
            case _ => Future.failed(NoAutorizado("Tipo usuario no existe"))
          }
          onComplete(resultado) {
            case Success(value) => execution(value)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  //TODO: Borrar este metodo cuando se realice la autorizacion de url para comerciales (Pendiente HU) By : Henando
  private def obtenerUsuarioComercialMock(tipoCliente: TiposCliente, usuario: String): Future[Autorizado] = Future {
    case class UsuarioLogged(id: Int, correo: String, identificacion: String, tipoIdentificacion: Int, tipoCliente: TiposCliente, usuario: String)
    val usuarioL = UsuarioLogged(0, "", "", 0, tipoCliente, usuario)
    val usuarioJson: String = JsonUtil.toJson(usuarioL)
    Autorizado(usuarioJson)
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case value: Autorizado => complete((StatusCodes.OK, value.usuario))
      case value: Prohibido => complete((StatusCodes.Forbidden, value.usuario))
      case ex: NoAutorizado => complete((StatusCodes.Unauthorized, ex))
      case ex: ValidacionException => complete((StatusCodes.Unauthorized, "sdf"))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: Throwable =>
        ex.printStackTrace()
        complete((StatusCodes.Unauthorized, "Error inesperado"))
    }
  }

  private def getTokenData(token: String): AuditityUser = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    val tipoCliente = nToken.getCustomClaim("tipoCliente").toString
    //TODO: el nit no lo pide si es tipo comercial
    val nit = if (tipoCliente == agente || tipoCliente == admin) nToken.getCustomClaim("nit").toString else ""
    val lastIp = nToken.getCustomClaim("ultimaIpIngreso").toString
    val user = nToken.getCustomClaim("nombreUsuario").toString
    val email = nToken.getCustomClaim("correo").toString
    val lastEntry = nToken.getCustomClaim("ultimaFechaIngreso").toString
    val nitType = nToken.getCustomClaim("tipoIdentificacion").toString
    AuditityUser(email, nit, nitType, user, lastIp, lastEntry, tipoCliente)
  }

}
