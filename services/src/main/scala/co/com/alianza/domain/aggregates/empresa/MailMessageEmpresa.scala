package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.infrastructure.dto.{PinEmpresa}
import co.com.alianza.mail.MailTemplate
import com.typesafe.config.Config

/**
 *
 * @author smontanez
 */
class MailMessageEmpresa(templateBody: String) extends MailTemplate {
  def getMessagePin(datos: PinEmpresa)(implicit config: Config): String = {
    val medida = "hora/s"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "medida" -> medida))
  }
}