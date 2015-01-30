package co.com.alianza.util.token

import co.com.alianza.commons.enumerations.TiposCliente
import org.joda.time.{DateTime}
import java.util.Date
import enumerations.AppendPasswordUser
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jose.crypto.MACVerifier

import collection.JavaConversions._


object Token {


  private val ISSUER = "http://fiduciaria.alianza.com.co"
  private val SIGNING_KEY = "556878763f1ea3bddfd1bed5b15daa2fc6d2db5a98290dd9f91ddfd22d77d1e" + AppendPasswordUser.appendUsuariosFiducia

  def generarToken(nombreUsuarioLogueado: String, correoUsuarioLogueado: String, tipoIdentificacion: String, ultimaIpIngreso: String, ultimaFechaIngreso: Date, expiracionInactividad: String, tipoCliente: TiposCliente.TiposCliente = TiposCliente.clienteIndividual, nit : Option[String] = None): String = {

    val claimsSet = new JWTClaimsSet()
    claimsSet.setIssueTime(new Date())
    claimsSet.setNotBeforeTime(new Date())
    claimsSet.setExpirationTime(new DateTime().plus(1800000).toDate)
    claimsSet.setIssuer(ISSUER)

    val formater = new java.text.SimpleDateFormat("dd MMMM, yyyy 'a las' hh:mm a", new java.util.Locale("es", "ES"))

    val customDataBase = Map(
      "correo" -> correoUsuarioLogueado,
      "nombreUsuario" -> nombreUsuarioLogueado,
      "tipoIdentificacion" -> tipoIdentificacion,
      "ultimaIpIngreso" -> ultimaIpIngreso,
      "ultimaFechaIngreso" -> formater.format(ultimaFechaIngreso),
      "tipoCliente" -> tipoCliente.toString,
      "expiracionInactividad" -> expiracionInactividad
    )

    val empresarialesData = nit match {
      case Some(x) => Map("nit" -> x)
      case None => Map()
    }
    val customData = customDataBase ++ empresarialesData

    claimsSet.setCustomClaims(customData)

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet)
    val signer = new MACSigner(SIGNING_KEY)
    signedJWT.sign(signer)
    val jwt = signedJWT.serialize()
    jwt
  }

  def generarTokenCaducidadContrasena(idUsuario: Int) = {

    val claimsSet = new JWTClaimsSet()
    claimsSet.setIssueTime(new Date())
    claimsSet.setNotBeforeTime(new Date())
    claimsSet.setExpirationTime(new DateTime().plus(1800000).toDate)
    claimsSet.setIssuer(ISSUER)

    val customData = Map(
      "us_id" -> idUsuario.toString
    )

    claimsSet.setCustomClaims(customData)

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet)
    val signer = new MACSigner(SIGNING_KEY)
    signedJWT.sign(signer)
    val jwt = signedJWT.serialize()
    jwt

  }

  def getToken(token: String): SignedJWT = {
    SignedJWT.parse(token)
  }

  def autorizarToken(token: String): Boolean = {
    try {
      val signedJWT2 = SignedJWT.parse(token)
      validarToken(signedJWT2)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        false
    }
  }

  private def validarToken(signedJWT2: SignedJWT) = {
    val verifier = new MACVerifier(SIGNING_KEY)
    val verify = signedJWT2.verify(verifier)
    verify match {
      case false => false
      case true => validarExpiracion(signedJWT2)
    }
  }

  private def validarExpiracion(signedJWT2: SignedJWT) = {
    val expirationTime = signedJWT2.getJWTClaimsSet.getExpirationTime
    val now = new Date()
    expirationTime.after(now)
  }

}

