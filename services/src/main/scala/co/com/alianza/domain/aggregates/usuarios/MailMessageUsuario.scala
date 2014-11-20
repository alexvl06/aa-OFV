package co.com.alianza.domain.aggregates.usuarios

import com.typesafe.config.Config
import co.com.alianza.infrastructure.dto.PinUsuario
import co.com.alianza.mail.MailTemplate

/**
 *
 * @author smontanez
 */
class MailMessageUsuario(templateBody: String) extends MailTemplate {
  def getMessagePin(datos: PinUsuario, numHorasCaducidad: Int)(implicit config: Config): String = {
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad))
  }
}

/*
object  MailMessageUsuario {
  def apply() = new MailMessageUsuario
}*/