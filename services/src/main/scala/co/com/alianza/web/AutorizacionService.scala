package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => AgenteEmpresarialDataAccessAdapter }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => ClienteAdminDataAccessAdapter }
import co.com.alianza.infrastructure.messages.{ AutorizarUrl, AutorizarUsuarioEmpresarialAdminMessage, AutorizarUsuarioEmpresarialMessage }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import spray.routing.Directives

case class AutorizacionService(
  kafkaActor: ActorSelection,
  autorizacionActor: ActorSelection,
  autorizacionUsuarioEmpresarialActor: ActorSelection
)(implicit val system: ActorSystem)
    extends Directives
    with AlianzaCommons {

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
                  requestExecute(AutorizarUsuarioEmpresarialAdminMessage(encryptedToken, Some(url)), autorizacionUsuarioEmpresarialActor) // Ya lo empece By :Alexa
                else
                  requestExecute(AutorizarUrl(encryptedToken, url), autorizacionActor)
            }
          }
        }
    }
  }

}