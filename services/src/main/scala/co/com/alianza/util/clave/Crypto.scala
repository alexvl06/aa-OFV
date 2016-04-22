package co.com.alianza.util.clave

import java.security.MessageDigest

/**
 *
 * @author smontanez
 */
object Crypto {

  def hashSha512(input: String, append: Int): String = {
    hashSha512(hashSha512(input: String) + append)
  }

  private def hashSha512(input: String): String = {
    val md = MessageDigest.getInstance("SHA-512")
    md.update(input.getBytes("UTF-8"))
    val mdbytes = md.digest()
    val hexString = new StringBuffer()
    for (i <- 0 to (mdbytes.length - 1)) {
      hexString.append(Integer.toString((mdbytes(i) & 0xff) + 0x100, 16).substring(1))
    }
    hexString.toString
  }

}
