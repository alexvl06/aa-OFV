package co.com.alianza.util.token

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.util.json.MarshallableImplicits._
import com.nimbusds.jose._
import com.nimbusds.jose.crypto.{ MACSigner, MACVerifier }
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.{ JWTClaimsSet, SignedJWT }
import enumerations.AppendPasswordUser
import org.joda.time.DateTime

import scala.collection.JavaConversions._

object Token {

  private val ISSUER = "http://fiduciaria.alianza.com.co"
  private val SIGNING_KEY = "556878763f1ea3bddfd1bed5b15daa2fc6d2db5a98290dd9f91ddfd22d77d1e" + AppendPasswordUser.appendUsuariosFiducia
  private val CORREO_DATA_NAME = "correo"
  private val NOMBRE_USUARIO_DATA_NAME = "nombreUsuario"
  private val TIPO_IDENTIFICACION_DATA_NAME = "tipoIdentificacion"
  private val ULTIMA_IP_INGRESO_DATA_NAME = "ultimaIpIngreso"
  private val ULTIMA_FECHA_INGRESO_DATA_NAME = "ultimaFechaIngreso"
  private val TIPO_CLIENTE = "tipoCliente"
  private val EXPIRACION_INACTIVIDAD = "expiracionInactividad"

  def generarToken(nombreUsuarioLogueado: String, correoUsuarioLogueado: String, tipoIdentificacion: String,
    ultimaIpIngreso: String, ultimaFechaIngreso: Date, expiracionInactividad: String,
    tipoCliente: TiposCliente.TiposCliente = TiposCliente.clienteIndividual, nit: Option[String] = None): String = {

    val claimsSet: JWTClaimsSet = getClaimSet()
    val formater = new java.text.SimpleDateFormat("dd MMMM, yyyy 'a las' hh:mm a", new java.util.Locale("es", "ES"))

    val customDataBase = Map(
      CORREO_DATA_NAME -> correoUsuarioLogueado,
      NOMBRE_USUARIO_DATA_NAME -> nombreUsuarioLogueado,
      TIPO_IDENTIFICACION_DATA_NAME -> tipoIdentificacion,
      ULTIMA_IP_INGRESO_DATA_NAME -> ultimaIpIngreso,
      ULTIMA_FECHA_INGRESO_DATA_NAME -> formater.format(ultimaFechaIngreso),
      TIPO_CLIENTE -> tipoCliente.toString,
      EXPIRACION_INACTIVIDAD -> expiracionInactividad
    )

    val empresarialesData = nit match {
      case Some(x) => Map("nit" -> x)
      case None => Map()
    }

    val customData = customDataBase ++ empresarialesData
    claimsSet.setCustomClaims(customData)

    val headersJWT: String = headersJWToken("HS512", "JWT").toJson
    val signedJWT = new SignedJWT(new JWSHeader(JWSHeader.parse(Base64URL.encode(headersJWT))), claimsSet)
    val signer: MACSigner = new MACSigner(SIGNING_KEY)
    signedJWT.sign(signer)
    signedJWT.serialize()
  }

  def generarTokenCaducidadContrasena(tipoUsuario: TiposCliente, idUsuario: Int) = {
    val claimsSet = new JWTClaimsSet()
    claimsSet.setIssueTime(new Date())
    claimsSet.setNotBeforeTime(new Date())
    claimsSet.setExpirationTime(new DateTime().plus(1800000).toDate)
    claimsSet.setIssuer(ISSUER)

    val customData = Map(
      "us_id" -> idUsuario.toString,
      "us_tipo" -> tipoUsuario.toString
    )

    claimsSet.setCustomClaims(customData)

    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claimsSet)
    val signer = new MACSigner(SIGNING_KEY)
    signedJWT.sign(signer)
    signedJWT.serialize()
  }

  def getToken(token: String): SignedJWT = {
    SignedJWT.parse(token)
  }

  def autorizarToken(token: String, validarExpiracionToken: Boolean = true): Boolean = {
    try {
      val signedJWT2 = SignedJWT.parse(token)
      validarToken(signedJWT2, validarExpiracionToken)
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
        false
    }
  }

  private def validarToken(signedJWT2: SignedJWT, validarExpiracionToken: Boolean): Boolean = {
    val verifier = new MACVerifier(SIGNING_KEY)
    val verify = signedJWT2.verify(verifier)
    verify match {
      case false => false
      case true => !validarExpiracionToken || validarExpiracion(signedJWT2)
    }
  }

  private def validarExpiracion(signedJWT2: SignedJWT): Boolean = {
    val expirationTime = signedJWT2.getJWTClaimsSet.getExpirationTime
    val now = new Date()
    expirationTime.after(now)
  }

  private def getClaimSet(): JWTClaimsSet = {
    val claimsSet = new JWTClaimsSet()
    claimsSet.setIssueTime(new Date())
    claimsSet.setNotBeforeTime(new Date())
    claimsSet.setExpirationTime(new DateTime().plus(1800000).toDate)
    claimsSet.setIssuer(ISSUER)
    claimsSet
  }

  private def getClaim(token: String, value: String): String = {
    val elements: SignedJWT = Token.getToken(token)
    val claimSet = elements.getJWTClaimsSet()
    claimSet.getStringClaim(value)
  }

}

case class headersJWToken(alg: String, typ: String)