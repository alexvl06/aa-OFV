package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.mail.MailTemplate
import co.com.alianza.persistence.entities.PinAgente
import com.typesafe.config.Config

/**
 *
 * @author smontanez
 */
class MailMessageEmpresa(templateBody: String) extends MailTemplate {

  def getMessagePin(datos: PinAgente, numHorasCaducidad: Int)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad, "medida" -> medida))
  }

  def getMessagePinCreacionAgente(datos: PinAgente, numHorasCaducidad: Int, usuario: String)(implicit config: Config): String = {
    val medida = if (numHorasCaducidad > 1) "horas" else "hora"
    engine.layout(config.getString(templateBody), Map("pin" -> datos.tokenHash, "numHorasCaducidad" -> numHorasCaducidad, "medida" -> medida, "usuario" -> usuario))
  }

}