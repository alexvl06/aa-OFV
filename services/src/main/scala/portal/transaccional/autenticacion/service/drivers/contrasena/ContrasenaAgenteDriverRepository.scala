package portal.transaccional.autenticacion.service.drivers.contrasena

import java.sql.Timestamp
import java.util.Calendar

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.domain.aggregates.empresa.MailMessageEmpresa
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.{ Configuracion, PinAgente }
import co.com.alianza.util.ConfigApp
import co.com.alianza.util.token.{ PinData, TokenPin }
import com.typesafe.config.Config
import enumerations.{ ConfiguracionEnum, EstadosEmpresaEnum, UsoPinEmpresaEnum }
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.smtp.{ Mensaje, SmtpRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioAgenteEmpresarialRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PinAgenteDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 10/11/16.
 */
case class ContrasenaAgenteDriverRepository(configuracionRepo: ConfiguracionRepository, smtpRepo: SmtpRepository,
    agenteRepo: UsuarioAgenteEmpresarialRepository,
    pinAgenteDAO: PinAgenteDAOs)(implicit val ex: ExecutionContext) extends ContrasenaAgenteRepository {

  private val config: Config = ConfigApp.conf

  def reiniciarContrasena(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean] = {
    for {
      _ <- validarAdmin(admin)
      optionAgente <- agenteRepo.getByIdentityAndUser(admin.identificacion, usuarioAgente)
      agente <- agenteRepo.validarUsuario(optionAgente)
      _ <- agenteRepo.validarBloqueoAdmin(agente)
      _ <- agenteRepo.actualizarEstado(agente.id, EstadosEmpresaEnum.pendienteReiniciarContrasena)
      expiracion <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_PIN)
      _ <- pinAgenteDAO.deleteAll(agente.id)
      pin <- obtenerPinAgente(agente.id, UsoPinEmpresaEnum.usoReinicioContrasena, expiracion)
      _ <- pinAgenteDAO.create(pin)
      mensaje <- obtenerMensaje(agente.correo, expiracion, pin)
      correo <- smtpRepo.enviar(mensaje)
    } yield correo
  }

  def cambiarEstado(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean] = {
    for {
      _ <- validarAdmin(admin)
      optionAgente <- agenteRepo.getByIdentityAndUser(admin.identificacion, usuarioAgente)
      agente <- agenteRepo.validarUsuario(optionAgente)
      _ <- agenteRepo.validarEstado(agente)
      //_ <- agenteRepo.actualizarEstado(agente.id, EstadosEmpresaEnum.pendienteReiniciarContrasena)
      //expiracion <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_PIN)
      //_ <- pinAgenteDAO.deleteAll(agente.id)
      //pin <- obtenerPinAgente(agente.id, UsoPinEmpresaEnum.usoReinicioContrasena, expiracion)
      //_ <- pinAgenteDAO.create(pin)
      //mensaje <- obtenerMensaje(agente.correo, expiracion, pin)
      //correo <- smtpRepo.enviar(mensaje)
    } yield correo
  }

  private def validarAdmin(admin: UsuarioAuth) = {
    admin.tipoCliente match {
      case TiposCliente.comercialAdmin => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.15", "El usuario no tiene permiso para realizar la acción"))
    }
  }

  private def obtenerPinAgente(idUsuario: Int, usoPin: UsoPinEmpresaEnum.Value, expiracion: Configuracion): Future[PinAgente] = {
    val fechaActual: Calendar = Calendar.getInstance()
    fechaActual.add(Calendar.HOUR_OF_DAY, expiracion.valor.toInt)
    val tokenPin: PinData = TokenPin.obtenerToken(fechaActual.getTime)
    val fecha = new Timestamp(tokenPin.fechaExpiracion.getTime)
    Future.successful(PinAgente(None, idUsuario, tokenPin.token, fecha, tokenPin.tokenHash.get, usoPin.id))
  }

  private def obtenerMensaje(para: String, expiracion: Configuracion, pin: PinAgente) = {
    val de: String = config.getString("alianza.smtp.from")
    val asunto: String = config.getString("alianza.smtp.asunto.reiniciarContrasenaEmpresa")
    val body: String = config.getString("alianza.smtp.templatepin.reiniciarContrasenaEmpresa")
    val contenido: String = new MailMessageEmpresa(body).getMessagePin(pin, expiracion.valor.toInt)
    //Future.successful(Mensaje(de, para, List.empty[String], asunto, contenido))
    Future.successful(Mensaje(de, "luisaceleita@seven4n.com", List.empty[String], asunto, contenido))
  }

}
