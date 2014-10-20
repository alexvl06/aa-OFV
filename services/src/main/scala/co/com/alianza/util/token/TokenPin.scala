package co.com.alianza.util.token

import org.apache.commons.lang3.RandomStringUtils
import java.security.MessageDigest
import java.util.Date


case class PinData(token:String, fechaExpiracion:Date, tokenHash:Option[String])

/**
 *
 * @author smontanez
 */
object TokenPin {
 def obtenerToken():PinData = {

   val md = MessageDigest.getInstance("SHA-512")

   val data: PinData = PinData(RandomStringUtils.randomAscii(89), new Date(System.currentTimeMillis()+86400000), None)
   val hash = md.digest(s"""${data.token} - ${data.fechaExpiracion}""".getBytes)

   val hexString = new StringBuffer()
   for (i <- hash) {
     hexString.append(Integer.toHexString(0xFF & i))
   }

   data.copy(tokenHash = Some(hexString.toString))
 }
}
