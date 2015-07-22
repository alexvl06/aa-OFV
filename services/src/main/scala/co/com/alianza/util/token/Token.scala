package co.com.alianza.util.token

import java.text.SimpleDateFormat

import com.nimbusds.jose.util.Base64URL
import org.joda.time.{DateTime}
import java.util.Date
import enumerations.AppendPasswordUser
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jose._
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jose.crypto.MACVerifier

import collection.JavaConversions._
import co.com.alianza.util.json.MarshallableImplicits._


object Token {


  private val ISSUER = "http://fiduciaria.alianza.com.co"
  private val SIGNING_KEY = "556878763f1ea3bddfd1bed5b15daa2fc6d2db5a98290dd9f91ddfd22d77d1e" + AppendPasswordUser.appendUsuariosFiducia
  private val CORREO_DATA_NAME = "correo"
  private val NOMBRE_USUARIO_DATA_NAME = "nombreUsuario"
  private val TIPO_IDENTIFICACION_DATA_NAME = "tipoIdentificacion"
  private val ULTIMA_IP_INGRESO_DATA_NAME = "ultimaIpIngreso"
  private val ULTIMA_FECHA_INGRESO_DATA_NAME = "ultimaFechaIngreso"

  def generarToken(nombreUsuarioLogueado: String, correoUsuarioLogueado: String, tipoIdentificacion: String, ultimaIpIngreso: String, ultimaFechaIngreso: Date): String = {

    val claimsSet = new JWTClaimsSet()
    claimsSet.setIssueTime(new Date())
    claimsSet.setNotBeforeTime(new Date())
    claimsSet.setExpirationTime(new DateTime().plus(1800000).toDate)
    claimsSet.setIssuer(ISSUER)

    val formater = new java.text.SimpleDateFormat("dd MMMM, yyyy 'a las' hh:mm a", new java.util.Locale("es", "ES"))

    val customData = Map(
      CORREO_DATA_NAME -> correoUsuarioLogueado,
      NOMBRE_USUARIO_DATA_NAME -> nombreUsuarioLogueado,
      TIPO_IDENTIFICACION_DATA_NAME -> tipoIdentificacion,
      ULTIMA_IP_INGRESO_DATA_NAME -> ultimaIpIngreso,
      ULTIMA_FECHA_INGRESO_DATA_NAME -> formater.format(ultimaFechaIngreso))

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

case class headersJWToken(alg: String, typ: String)
case class dataJWToken(iat: Date, nbf: Date, exp: Date, iss: String, tipoIdentificacion: String, ultimaIpIngreso: String, nombreUsuarioLogueado: String, correo: String, ultimaFechaIngreso: String)