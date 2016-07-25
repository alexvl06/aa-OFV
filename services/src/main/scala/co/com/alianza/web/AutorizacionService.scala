package co.com.alianza.web

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.auditing.{ AuditingHelper, AuditingUser }
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.messages.AutorizarUsuarioEmpresarialMessage
import co.com.alianza.infrastructure.messages.AutorizarUsuarioEmpresarialAdminMessage
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => AgenteEmpresarialDataAccessAdapter }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => ClienteAdminDataAccessAdapter }
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.util.token.{ AesUtil, Token }
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import enumerations.CryptoAesParameters
import spray.routing.{ Directives, RequestContext }
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{ AutorizarUrl, InvalidarToken }
import co.com.alianza.infrastructure.cache.CacheHelper
import akka.actor.{ ActorSelection, ActorSystem }

import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.cache.CachingDirectiveAlianza

import scala.concurrent.Future
import scalaz.Validation

case class AutorizacionService (kafkaActor: ActorSelection) extends Directives with AlianzaCommons with CacheHelper {

  import CachingDirectiveAlianza._
  implicit val system: ActorSystem = MainActors.system
  implicit val contextAuthorization: ExecutionContext = MainActors.ex
  implicit val conf: Config = MainActors.conf

  import AutenticacionMessagesJsonSupport._

  def route = {
    path("validarToken" / Segment) {
      token =>
        get {
          respondWithMediaType(mediaType) {
            parameters('url, 'ipRemota) {
              (url, ipRemota) =>

                val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
                var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
                var encryptedToken = util.encrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)

                if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
                  requestExecute(AutorizarUsuarioEmpresarialMessage(encryptedToken, Some(url), ipRemota), autorizacionUsuarioEmpresarialActor)
                else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
                  requestExecute(AutorizarUsuarioEmpresarialAdminMessage(encryptedToken, Some(url)), autorizacionUsuarioEmpresarialActor)
                else
                  requestExecute(AutorizarUrl(encryptedToken, url), autorizacionActor)
            }
          }
        }
    } ~ path("invalidarToken") {
      entity(as[InvalidarToken]) {
        token =>
          delete {
            respondWithMediaType(mediaType) {
              var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
              var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token.token)
              clientIP { ip =>
                mapRequestContext {
                  r: RequestContext =>
                    val tipoCliente = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
                    val usuario: Future[Validation[PersistenceException, Option[AuditingUser.AuditingUserData]]] = if (tipoCliente == TiposCliente.agenteEmpresarial.toString) {
                      AgenteEmpresarialDataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.token)
                    } else if (tipoCliente == TiposCliente.clienteAdministrador.toString) {
                      ClienteAdminDataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.token)
                    } else {
                      DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.token)
                    }
                    requestWithFutureAuditing[PersistenceException, Usuario](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cierreSesionIndex, ip.value, kafkaActor, usuario)
                } {
                  val tipoCliente = Token.getToken(decryptedToken).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
                  if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
                    requestExecute(InvalidarTokenAgente(token.token), autorizacionActor)
                  else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
                    requestExecute(InvalidarTokenClienteAdmin(token.token), autorizacionActor)
                  else
                    requestExecute(InvalidarToken(token.token), autorizacionActor)
                }
              }
            }
          }
      }
    }
  }
}