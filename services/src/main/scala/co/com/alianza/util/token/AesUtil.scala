package co.com.alianza.util.token

import enumerations.CryptoAesParameters

/**
 * Created by hernando on 3/08/16.
 */
object AesUtil {

  private val aes = new Aes(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)

  def encriptarToken(token: String): String = {
    aes.encrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
  }

  def desencriptarToken(encriptedToken: String): String = {
    aes.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, encriptedToken)
  }

}
