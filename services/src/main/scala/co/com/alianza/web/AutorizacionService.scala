package co.com.alianza.web

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.util.token.Token
import spray.routing.Directives
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.{AutorizarUsuarioEmpresarialMessage, AutorizarUsuarioEmpresarialAdminMessage, InvalidarToken, AutorizarUrl}
import co.com.alianza.infrastructure.cache.CacheHelper
import akka.actor.ActorSystem
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.cache.CachingDirectiveAlianza

class AutorizacionService extends Directives with AlianzaCommons with CacheHelper {


  import CachingDirectiveAlianza._

  implicit val system: ActorSystem = MainActors.system
  implicit val contextAuthorization: ExecutionContext = MainActors.ex
  implicit val conf: Config = MainActors.conf

  def route = {

    path("validarToken" / Segment) {
      token =>
        get {
          respondWithMediaType(mediaType) {
            parameters('url, 'ipRemota) {
              (url, ipRemota) =>

                val tipoCliente = Token.getToken(token).getJWTClaimsSet.getCustomClaim("tipoCliente").toString

                if (tipoCliente == TiposCliente.agenteEmpresarial.toString)
                  cacheAlianza()
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
            requestExecute(InvalidarToken(token), autorizacionActor)
          }
        }

    }
  }
}



