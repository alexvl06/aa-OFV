package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import akka.actor.ActorSystem
import co.com.alianza.domain.aggregates.empresa.MailMessageEmpresa
import co.com.alianza.domain.aggregates.pin.PinUtil
import co.com.alianza.microservices.{ MailMessage, SmtpServiceClient }
import co.com.alianza.persistence.entities.{ Configuraciones, PinAgenteInmobiliario }
import co.com.alianza.util.token.{ PinData, TokenPin }
import com.typesafe.config.Config
import enumerations.EstadosPin._
import enumerations.UsoPinEmpresaEnum
import org.joda.time.{ DateTime, DateTimeZone }
import org.joda.time.{ DateTime, DateTimeZone }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PinAgenteInmobiliarioDAOs

import scala.concurrent.{ ExecutionContext, Future }

case class UsuarioInmobiliarioPinDriverRepository(pinDao: PinAgenteInmobiliarioDAOs) extends UsuarioInmobiliarioPinRepository {

  override def asociarPinAgente(pinAgente: PinAgenteInmobiliario): Future[Option[Int]] = {
    pinDao.create(pinAgente)
  }

  override def validarPinAgente(hash: String)(implicit ex: ExecutionContext): Future[Either[EstadoPin, PinAgenteInmobiliario]] = {
    pinDao.get(hash).map(pin => PinUtil.validarPinAgenteInmobiliario(pin))
  }

  override def generarPinAgente(configExpiracion: Configuraciones, idUsuario: Int, reinicio: Boolean = false): PinAgenteInmobiliario = {
    val fechaExpiracion: DateTime = new DateTime(DateTimeZone.UTC).plusHours(configExpiracion.valor.toInt)
    val pin: PinData = TokenPin.obtenerToken(fechaExpiracion.toDate)
    val usoPin: Int = reinicio match {
      case true => UsoPinEmpresaEnum.usoReinicioContrasena.id
      case _ => UsoPinEmpresaEnum.creacionAgenteInmobiliario.id
    }
    PinAgenteInmobiliario(None, idUsuario, pin.token, fechaExpiracion, pin.tokenHash.getOrElse(""), usoPin)
  }

  override def generarCorreoActivacion(pin: String, caducidad: Int, nombreAgente: String, usuarioAgente: String,
    correo: String)(implicit config: Config): MailMessage = {
    val remitente: String = config.getString("alianza.smtp.from")
    val asunto: String = config.getString("alianza.smtp.asunto.creacionAgenteInmobiliario")
    val cuerpo: String = new MailMessageEmpresa("alianza.smtp.templatepin.creacionAgenteInmobiliario")
      .getMessageAgenteInmobiliario(pin, caducidad, nombreAgente, Some(usuarioAgente))
    MailMessage(remitente, correo, Nil, asunto, cuerpo, "")
  }

  override def generarCorreoReinicio(pin: String, caducidad: Int, nombreAgente: String, correo: String)(implicit config: Config): MailMessage = {
    val remitente: String = config.getString("alianza.smtp.from")
    val asunto: String = config.getString("alianza.smtp.asunto.reiniciarContrasenaAgenteInmobiliario")
    val cuerpo: String = new MailMessageEmpresa("alianza.smtp.templatepin.reiniciarContrasenaAgenteInmobiliario")
      .getMessageAgenteInmobiliario(pin, caducidad, nombreAgente, None)
    MailMessage(remitente, correo, Nil, asunto, cuerpo, "")
  }

  override def enviarEmail(correo: MailMessage)(implicit actorySystem: ActorSystem): Unit = {
    new SmtpServiceClient().send(correo, (_, _) => 1)
    ()
  }

  override def eliminarPinAgente(hash: String): Future[Int] = {
    pinDao.delete(hash)
  }
}
