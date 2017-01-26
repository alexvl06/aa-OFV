package co.com.alianza.util.token

import java.text.SimpleDateFormat

import org.apache.commons.lang3.RandomStringUtils
import java.security.MessageDigest
import java.util.Date

case class PinData(token: String, fechaExpiracion: Date, tokenHash: Option[String])

/**
 *
 * @author smontanez
 */
object TokenPin {

  def obtenerToken(fechaExp: Date): PinData = {
    val md = MessageDigest.getInstance("SHA-512")
    val data: PinData = PinData(RandomStringUtils.randomAscii(89), fechaExp, None)
    //formatear fecha
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val fechaFormato: String = format.format(fechaExp)
    val hash = md.digest(s"""${data.token} - ${fechaFormato}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    data.copy(tokenHash = Some(hexString.toString))
  }

}
