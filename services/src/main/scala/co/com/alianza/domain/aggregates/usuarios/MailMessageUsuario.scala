package co.com.alianza.domain.aggregates.usuarios

import com.typesafe.config.Config
import co.com.alianza.infrastructure.dto.{PinUsuarioEmpresarialAdmin, PinUsuario}
import co.com.alianza.mail.MailTemplate

/**
 *
 * @author smontanez
 */
class MailMessageUsuario(templateBody: String) extends MailTemplate {
  def getMessagePin(datos: PinUsuario, numHorasCaducidad: Int, ut: String, funcionalidad:String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad, "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad))
  }
  def getMessagePin(datos: PinUsuarioEmpresarialAdmin, numHorasCaducidad: Int, ut: String, funcionalidad:String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad, "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad))
  }
}

