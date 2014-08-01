package co.com.alianza.util.token


import spray.routing.authentication.{Authentication, ContextAuthenticator}
import net.oauth.jsontoken.crypto.{HmacSHA256Signer, HmacSHA256Verifier}
import org.joda.time.Duration
import net.oauth.jsontoken.SystemClock
import net.oauth.jsontoken.JsonToken
import net.oauth.jsontoken.discovery.VerifierProviders
import net.oauth.jsontoken.crypto.SignatureAlgorithm
import net.oauth.jsontoken.JsonTokenParser
import java.security.SignatureException
import net.oauth.jsontoken.discovery.VerifierProvider
import net.oauth.jsontoken.crypto.Verifier
import java.util.ArrayList
import java.util.Date
import com.google.gson.JsonParseException


object Token{

    def generarToken( nombreUsuarioLogueado:String, correoUsuarioLogueado:String, tipoIdentificacion:String, ultimaIpIngreso: String, ultimaFechaIngreso: String ) : String = {
      val SKEW = Duration.standardMinutes(1)
      val signer = new HmacSHA256Signer("google.com", "key2", "kjdhasdkjhaskdjhaskdjhaskdjh".getBytes())
      val clock = new SystemClock()
      val token = new JsonToken(signer)
      token.setParam("correo", correoUsuarioLogueado)
      token.setParam("nombreUsuario", nombreUsuarioLogueado)
      token.setParam("tipoIdentificacion", tipoIdentificacion)
      token.setParam("ultimaIpIngreso", ultimaIpIngreso)
      token.setParam("ultimaFechaIngreso", ultimaFechaIngreso)
      //TODO: Se debe cambiar el Audience al dominio dado para ambiente de produccion
      token.setAudience("http://www.google.com")
      token.setIssuedAt(clock.now())
      token.setExpiration(clock.now().plus(1800000))
      val encodedJWT = token.serializeAndSign()
      //    	val jwt2 = new JSONWebToken()
      //        jwt2.setPrivateKey(keyPair.getPrivate())
      //        jwt2.load(encodedJWT)
      /*println(token.getExpiration().toDateTime())
      println(" Token " + encodedJWT)*/
      encodedJWT
    }

    def  autorizarToken(token:String) : Boolean = {
      //println("Token- " + tokenHeder)
      val SKEW = Duration.standardMinutes(1)
      val clock = new SystemClock(SKEW)
      val locators = new VerifierProviders()
      locators.setVerifierProvider(SignatureAlgorithm.HS256, verificador.hmacLocator)
      val parser = new JsonTokenParser(clock, locators, new checkerAutorizacion())
      try{
        val tokenVerified = parser.verifyAndDeserialize(token)
        println("Expiracion de Token " + tokenVerified.getExpiration() + " ahora " + new Date())
        //	    if(token.getExpiration().isAfter(new Instant())){
        //	      false
        //	    }else
        true
      }catch{
        case ex: SignatureException =>
          println("Error SignatureException " + ex.getMessage())
          false
        case ex: IllegalStateException =>
          println("Error IllegalStateException  " + ex.getMessage())
          false
        case ex: JsonParseException =>
          println("Error JsonParseException  " + ex.getMessage())
          false
      }
    }

  object verificador {
    val hmacVerifier = new HmacSHA256Verifier("kjdhasdkjhaskdjhaskdjhaskdjh".getBytes())
    val hmacLocator = new VerifierProvider() {
      def findVerifier(arg0 : String, arg1 : String) : ArrayList[Verifier] = {
        //	    List(hmacVerifier)
        val lista = new ArrayList[Verifier]()
        lista.add(hmacVerifier)
        lista
      }
    }
  }






  }

