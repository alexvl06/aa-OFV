package co.com.alianza.domain.aggregates.usuarios

import co.com.alianza.persistence.entities.{ PinAdmin, PinAgente, PinUsuario }
import com.typesafe.config.Config
import co.com.alianza.mail.MailTemplate

/**
 *
 * @author smontanez
 */
class MailMessageUsuario(templateBody: String) extends MailTemplate {
  def getMessagePin(datos: PinUsuario, numHorasCaducidad: Int, ut: String, funcionalidad: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad, "dominio" -> config.getString("alianza.smtp.domain")))
  }
  def getMessagePin(datos: PinAdmin, numHorasCaducidad: Int, ut: String, funcionalidad: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad, "dominio" -> config.getString("alianza.smtp.domain")))
  }
  def getMessagePin(datos: PinAgente, numHorasCaducidad: Int, ut: String, funcionalidad: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad, "dominio" -> config.getString("alianza.smtp.domain")))
  }

  def getMessagePin(datos: PinUsuario, numHorasCaducidad: Int, ut: String, funcionalidad: String, idFormulario: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad, "dominio" -> config.getString("alianza.smtp.domain"), "idFormulario" -> idFormulario))
  }

  def getMessagePin(datos: PinAgente, numHorasCaducidad: Int, ut: String, funcionalidad: String, idFormulario: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> medida, "ut" -> ut, "ft" -> funcionalidad, "dominio" -> config.getString("alianza.smtp.domain"), "idFormulario" -> idFormulario))
  }
}

