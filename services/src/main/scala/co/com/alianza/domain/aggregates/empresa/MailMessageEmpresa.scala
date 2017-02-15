package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.mail.MailTemplate
import co.com.alianza.persistence.entities.PinAgente
import com.typesafe.config.Config

class MailMessageEmpresa(templateBody: String) extends MailTemplate {

  def getMessagePin(datos: PinAgente, numHorasCaducidad: Int)(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> datos.tokenHash,
      "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> getMedidaCaducidad(numHorasCaducidad),
      "dominio" -> config.getString("alianza.smtp.domain")
    )
    engine.layout(config.getString(templateBody), mailParams)
  }

  def getMessagePinCreacionAgente(datos: PinAgente, numHorasCaducidad: Int, usuario: String)(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> datos.tokenHash,
      "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> getMedidaCaducidad(numHorasCaducidad),
      "usuario" -> usuario,
      "dominio" -> config.getString("alianza.smtp.domain")
    )
    engine.layout(config.getString(templateBody), mailParams)
  }

  def getMessageAgenteInmobiliario(pin: String, caducidad: Int, nombreAgente: String, usuarioAgente: Option[String])(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> pin,
      "numHorasCaducidad" -> caducidad,
      "medida" -> getMedidaCaducidad(caducidad),
      "nombre" -> nombreAgente,
      "dominio" -> config.getString("alianza.smtp.domain")
    )
    usuarioAgente match {
      case None => engine.layout(config.getString(templateBody), mailParams)
      case Some(us) => engine.layout(config.getString(templateBody), mailParams + ("usuario" -> us))
    }
  }

  private def getMedidaCaducidad(caducidad: Int): String = if (caducidad > 1) "horas" else "hora"
}