package co.com.alianza.util.token

import enumerations.CryptoAesParameters

/**
 * Created by hernando on 3/08/16.
 */
object AesUtil {

  private val aes = new Aes(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)

  def encriptarToken(token: String, nombreClase : String): String = {
    val n = aes.encrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    n
  }

  def desencriptarToken(encriptedToken: String, nombreClase : String): String = {
    val n = aes.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, encriptedToken)
    n
  }

}
