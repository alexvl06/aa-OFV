package portal.transaccional.autenticacion.service.web.autorizacion

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.UsuarioInmobiliarioAuth
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import co.com.alianza.util.token.{ AesUtil, Token }
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.AutorizacionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.autenticacion.service.drivers.util.SesionAgenteUtilRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.{ CommonRESTFul, GenericAutorizado, GenericNoAutorizado }
import spray.http.{ MediaTypes, StatusCodes }
import spray.routing.{ RequestContext, Route, StandardRoute }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

/**
 * Created by s4n 2016
 */
case class AutorizacionService(
    kafkaActor: ActorSelection,
    usuarioRepository: UsuarioRepository,
    usuarioAgenteRepository: UsuarioEmpresarialRepository[UsuarioEmpresarial],
    usuarioAdminRepository: UsuarioAdminRepository,
    autorizacionRepository: AutorizacionUsuarioRepository,
    autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository,
    autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository,
    autorizacionComercialRepo: AutorizacionUsuarioComercialRepository,
    autorizacionComercialAdminRepo: AutorizacionUsuarioComercialAdminRepository,
    sesionUtilAgenteInmobiliario: SesionAgenteUtilRepository,
    autorizacionAgenteInmob: AutorizacionRepository
)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters with CrossHeaders {

  val invalidarTokenPath = "invalidarToken"
  val validarTokenPath = "validarToken"
  val validarTokenInmobiliarioPath = "validarTokenInmobiliario"
  /**OFV LOGIN FASE 1**/
  val validarTokenGeneralPath = "validarToken-general"
  val invalidarTokenGeneralPath = "invalidarToken-general"

  //tipos clientes
  val agente = TiposCliente.agenteEmpresarial.toString
  val comercialSAC = TiposCliente.comercialSAC.toString
  val admin = TiposCliente.clienteAdministrador.toString
  val individual = TiposCliente.clienteIndividual.toString
  val comercialAdmin = TiposCliente.comercialAdmin.toString
  val comercialValores = TiposCliente.comercialValores.toString
  val comercialFiduciaria = TiposCliente.comercialFiduciaria.toString
  val adminInmobiliaria = TiposCliente.clienteAdminInmobiliario.toString
  val agenteInmobiliario = TiposCliente.agenteInmobiliario.toString
  val agenteInmobiliarioInterno = TiposCliente.agenteInmobiliarioInterno.toString

  val route: Route = {
    path(invalidarTokenPath) {
      pathEndOrSingleSlash {
        invalidarToken
      }
    } ~ path(validarTokenPath / Segment) {
      token => validarToken(token)
    } ~ path(validarTokenInmobiliarioPath) {
      validarTokenInmobiliario()
    } ~ path(validarTokenGeneralPath) {
      /**OFV LOGIN FASE 1**/
      validarTokenGeneral()
    } ~ path(invalidarTokenGeneralPath) {
      invalidarTokenGen
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
              case `agenteInmobiliario` | `agenteInmobiliarioInterno` => sesionUtilAgenteInmobiliario.invalidarToken(token, encriptedToken)
              case `admin` | `adminInmobiliaria` => autorizacionAdminRepo.invalidarToken(token, encriptedToken)
              case `agente` => autorizacionAgenteRepo.invalidarToken(token, encriptedToken)
              case `agenteInmobiliario` => sesionUtilAgenteInmobiliario.invalidarToken(token, encriptedToken)
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
            case `admin` | `adminInmobiliaria` => autorizacionAdminRepo.autorizar(token, encriptedToken, url, ipRemota, `admin`)
            case `individual` => autorizacionRepository.autorizar(token, encriptedToken, url)
            case `agenteInmobiliario` | `agenteInmobiliarioInterno` => autorizacionAgenteInmob.autorizar(token, encriptedToken, Option(url), ipRemota, usuario.tipoCliente)
            case `comercialSAC` => autorizacionComercialRepo.autorizarSAC(token, encriptedToken, url)
            case `comercialFiduciaria` => autorizacionComercialRepo.autorizarFiduciaria(token, encriptedToken, url)
            case `comercialValores` => autorizacionComercialRepo.autorizarValores(token, encriptedToken, url)
            case `comercialAdmin` => autorizacionComercialAdminRepo.autorizar(token, encriptedToken, url)
            case _ => Future.failed(NoAutorizado("Tipo usuario no existe"))
          }
          onComplete(resultado) {
            case Success(value) => execution(value)
            case Failure(ex) => execution(ex)
          }
      }
    }
  }

  private def validarTokenInmobiliario() = {
    get {
      clientIP { ipRemota =>
        headerValueByName("token") { token =>
          parameters('url) { (url) =>
            respondWithMediaType(MediaTypes.`application/json`) {
              val decriptedToken: String = AesUtil.desencriptarToken(token)
              val usuario: AuditityUser = getTokenData(decriptedToken)

              val responseF = usuario.tipoCliente match {
                case `adminInmobiliaria` => autorizacionAdminRepo.autorizar(decriptedToken, token, url, ipRemota.value, `adminInmobiliaria`)
                case `agenteInmobiliario` | `agenteInmobiliarioInterno` => autorizacionAgenteInmob.autorizar(decriptedToken, token, Option(url), ipRemota.value, usuario.tipoCliente)
                case _ => Future.failed(GenericNoAutorizado("403", "Rol no autorizado para acceder a esta funcionalidad"))
              }

              onComplete(responseF) {
                case Success(value) => execution(value)
                case Failure(ex) => execution(ex)
              }
            }
          }
        }
      }
    }
  }

  /**OFV LOGIN FASE 1**/
  private def validarTokenGeneral() = {
    get {
      clientIP { ipRemota =>
        headerValueByName("token") { tokenEncripted =>
          headerValueByName("url") { (url) =>
            respondWithMediaType(MediaTypes.`application/json`) {
              val token: String = AesUtil.desencriptarToken(tokenEncripted)
              val usuario: AuditityUser = getTokenData(token)
              val response = autorizacionRepository.autorizarGeneral(token, url)
              onComplete(response) {
                case Success(value) => execution(value)
                case Failure(ex) => execution(ex)
              }
            }
          }
        }
      }
    }
  }
  /**OFV LOGIN FASE 1**/
  private def invalidarTokenGen = {
    entity(as[InvalidarTokenRequest]) {
      tokenRequest =>
        delete {
          clientIP { ip =>
            val encriptedToken: String = tokenRequest.token
            val token: String = AesUtil.desencriptarToken(encriptedToken)
            val usuario = getTokenData(token)
            val resultado: Future[Any] = autorizacionRepository.invalidarToken(token, encriptedToken)
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

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: Autorizado => complete((StatusCodes.OK, ex.usuario))
      case ex: AutorizadoComercial => complete((StatusCodes.OK, ex.usuario))
      case ex: AutorizadoComercialAdmin => complete((StatusCodes.OK, ex.usuario))
      case ex: Prohibido => complete((StatusCodes.Forbidden, ex.usuario))
      case ex: NoAutorizado => complete((StatusCodes.Unauthorized, ex))
      case ex: ValidacionException => complete((StatusCodes.Unauthorized, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: GenericNoAutorizado => complete((StatusCodes.Forbidden, ex))
      case ex: GenericAutorizado[UsuarioInmobiliarioAuth] => complete((StatusCodes.OK, ex.usuario))
      case ex: Throwable => complete((StatusCodes.Unauthorized, "Error inesperado"))
    }
  }

  private def getTokenData(token: String): AuditityUser = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    val tipoCliente = nToken.getCustomClaim("tipoCliente").toString
    //TODO: el nit no lo pide si es tipo comercial
    val nit = if (tipoCliente == agente || tipoCliente == admin || tipoCliente == adminInmobiliaria || tipoCliente == agenteInmobiliario || tipoCliente == agenteInmobiliarioInterno) nToken.getCustomClaim("nit").toString else ""
    val lastIp = nToken.getCustomClaim("ultimaIpIngreso").toString
    val user = nToken.getCustomClaim("nombreUsuario").toString
    val email = nToken.getCustomClaim("correo").toString
    val lastEntry = nToken.getCustomClaim("ultimaFechaIngreso").toString
    val nitType = nToken.getCustomClaim("tipoIdentificacion").toString
    AuditityUser(email, nit, nitType, user, lastIp, lastEntry, tipoCliente)
  }

}
