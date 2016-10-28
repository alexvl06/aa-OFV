package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.infrastructure.dto.PinEmpresa
import co.com.alianza.mail.MailTemplate
import com.typesafe.config.Config

class MailMessageEmpresa(templateBody: String) extends MailTemplate {

  def getMessagePin(datos: PinEmpresa, numHorasCaducidad: Int)(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> datos.tokenHash,
      "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> getMedidaCaducidad(numHorasCaducidad)
    )
    engine.layout(config.getString(templateBody), mailParams)
  }

  def getMessagePinCreacionAgente(datos: PinEmpresa, numHorasCaducidad: Int, usuario: String)(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> datos.tokenHash,
      "numHorasCaducidad" -> numHorasCaducidad,
      "medida" -> getMedidaCaducidad(numHorasCaducidad),
      "usuario" -> usuario
    )
    engine.layout(config.getString(templateBody), mailParams)
  }

  def getMessageAgenteInmobiliario(pin: String, caducidad: Int, usuario: Option[String])(implicit config: Config): String = {
    val mailParams: Map[String, Any] = Map(
      "pin" -> pin,
      "numHorasCaducidad" -> caducidad,
      "medida" -> getMedidaCaducidad(caducidad)
    )
    usuario match {
      case None => engine.layout(config.getString(templateBody), mailParams)
      case Some(us) => engine.layout(config.getString(templateBody), mailParams + ("usuario" -> us))
    }
  }

  private def getMedidaCaducidad(caducidad: Int): String = if (caducidad > 1) "horas" else "hora"
}