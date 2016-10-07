package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import akka.actor.ActorSystem
import co.com.alianza.domain.aggregates.empresa.MailMessageEmpresa
import co.com.alianza.domain.aggregates.pin.PinUtil
import co.com.alianza.microservices.{MailMessage, SmtpServiceClient}
import co.com.alianza.persistence.entities.{Configuraciones, PinAgenteInmobiliario}
import co.com.alianza.util.token.{PinData, TokenPin}
import com.typesafe.config.Config
import enumerations.EstadosPin._
import enumerations.UsoPinEmpresaEnum
import org.joda.time.DateTime
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PinAgenteInmobiliarioDAOs

import scala.concurrent.{ExecutionContext, Future}

case class UsuarioInmobiliarioPinDriverRepository(pinDao: PinAgenteInmobiliarioDAOs) extends UsuarioInmobiliarioPinRepository {

  override def asociarPinAgente(pinAgente: PinAgenteInmobiliario): Future[Option[Int]] = {
    pinDao.create(pinAgente)
  }

  override def validarPinAgente(hash: String)(implicit ex: ExecutionContext): Future[Either[EstadoPin, Boolean]] = {
    pinDao.get(hash).map(pin => PinUtil.validarPinAgenteInmobiliario(pin))
  }

  override def generarPinAgente(configExpiracion: Configuraciones, idUsuario: Int): PinAgenteInmobiliario = {
    val fechaExpiracion: DateTime = new DateTime().plusHours(configExpiracion.valor.toInt)
    val (pin: PinData, usoPin: Int) = (TokenPin.obtenerToken(fechaExpiracion.toDate), UsoPinEmpresaEnum.creacionAgenteInmobiliario.id)
    PinAgenteInmobiliario(None, idUsuario, pin.token, fechaExpiracion, pin.tokenHash.getOrElse(""), usoPin)
  }

  override def generarCorreoActivacion(pin: String, caducidad: Int, identificacion: String,
                                       usuario: String, correo: String)(implicit config: Config): MailMessage = {
    val remitente: String = config.getString("alianza.smtp.from")
    val asunto: String = config.getString("alianza.smtp.asunto.creacionAgenteInmobiliario")
    val cuerpo: String = new MailMessageEmpresa("alianza.smtp.templatepin.creacionAgenteInmobiliario")
      .getMessagePinCreacionAgenteInmobiliario(pin, caducidad, identificacion, usuario)
    MailMessage(remitente, correo, Nil, asunto, cuerpo, "")
  }

  override def enviarEmail(correo: MailMessage)(implicit actorySystem: ActorSystem): Unit = {
    new SmtpServiceClient().send(correo, (_, _) => 1)
    ()
  }
}
