package co.com.alianza.domain.aggregates.pin

import java.security.MessageDigest
import java.util.Date

object PinUtil {

  def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(s"""${pin} - ${fechaExpiracion}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    hexString.toString
  }

}
