package co.com.alianza.util.clave

import java.security.MessageDigest

/**
 *
 * @author smontanez
 */
object Crypto {
  def hashSha256(input:String):String={
    val md = MessageDigest.getInstance("SHA-256")
    md.update(input.getBytes("UTF-8"))
    val mdbytes = md.digest()
    val hexString = new StringBuffer()
    for (i <- 0 to (mdbytes.length-1)) {
      hexString.append(Integer.toString((mdbytes(i) & 0xff) + 0x100, 16).substring(1))
    }

    hexString.toString
  }

}
