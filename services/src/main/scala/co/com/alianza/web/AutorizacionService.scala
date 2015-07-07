package co.com.alianza.web

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.util.token.Token
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.cache.CacheHelper
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.cache.CachingDirectiveAlianza
import co.com.alianza.infrastructure.messages.InvalidarToken
import co.com.alianza.infrastructure.messages.AutorizarUrl
import co.com.alianza.infrastructure.messages.AutorizarUsuarioEmpresarialMessage
import co.com.alianza.infrastructure.messages.AutorizarUsuarioEmpresarialAdminMessage
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import spray.routing.{RequestContext, Directives}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.util.token.Token
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.util.clave.Crypto
import com.nimbusds.jwt.{SignedJWT, JWTClaimsSet}
import enumerations.AppendPasswordUser
import spray.routing.{RequestContext, Directives}
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{InvalidarToken, AutorizarUrl}
import co.com.alianza.infrastructure.cache.CacheHelper
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.cache.CachingDirectiveAlianza

class AutorizacionService extends Directives with AlianzaCommons with CacheHelper{

  import CachingDirectiveAlianza._
  implicit val system: ActorSystem = MainActors.system
  implicit val contextAuthorization: ExecutionContext = MainActors.ex
  implicit val conf: Config= MainActors.conf

  def route = {
    path("validarToken" / Segment) {
      token =>
        get {
          respondWithMediaType(mediaType) {
            parameters('url, 'ipRemota) {
              (url, ipRemota) =>

                val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

                if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
                  requestExecute(AutorizarUsuarioEmpresarialMessage(token, Some(url), ipRemota), autorizacionUsuarioEmpresarialActor)
                else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
                  requestExecute(AutorizarUsuarioEmpresarialAdminMessage(token, Some(url)), autorizacionUsuarioEmpresarialActor)
                else
                  requestExecute(AutorizarUrl(token, url), autorizacionActor)
             }
          }
        }
    } ~ path("invalidarToken" / Segment) {
      token =>
        get {
          respondWithMediaType(mediaType) {
            clientIP { ip =>
              mapRequestContext {
                r: RequestContext =>
                  val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString
                  val usuario = DataAccessAdapter.obtenerUsuarioToken(token)
                  requestWithFutureAuditing[PersistenceException, Usuario](r, "Fiduciaria", "cierre-sesion-fiduciaria", ip.value, kafkaActor, usuario)
              } {

                val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

                if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
                  requestExecute(InvalidarTokenAgente(token), autorizacionActor)
                else if (tipoCliente == TiposCliente.clienteAdministrador.toString)
                  requestExecute(InvalidarTokenClienteAdmin(token), autorizacionActor)
                else
                  requestExecute(InvalidarToken(token), autorizacionActor)
              }
            }
          }
	      }
    }
  }
}



