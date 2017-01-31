package portal.transaccional.autenticacion.service.drivers.contrasena

import java.sql.Timestamp
import java.util.{ Calendar, Date }

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.constants.LlavesReglaContrasena
import co.com.alianza.domain.aggregates.empresa.MailMessageEmpresa
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities._
import co.com.alianza.util.ConfigApp
import co.com.alianza.util.clave.Crypto
import co.com.alianza.util.token.{ PinData, TokenPin }
import com.typesafe.config.Config
import enumerations._
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.smtp.{ Mensaje, SmtpRepository }
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteEmpresarial.UsuarioEmpresarialDriverRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.PinAgenteDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 10/11/16.
 */
case class ContrasenaAgenteDriverRepository(
    agenteRepo: UsuarioEmpresarialDriverRepository,
    pinAgenteDAO: PinAgenteDAOs,
    ultimaContrasenaRepo: UltimaContrasenaRepository,
    configuracionRepo: ConfiguracionRepository,
    smtpRepo: SmtpRepository,
    reglaRepo: ReglaContrasenaRepository
)(implicit val ex: ExecutionContext) extends ContrasenaAgenteRepository {

  private implicit val config: Config = ConfigApp.conf

  def reiniciarContrasena(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean] = {
    for {
      _ <- validarAdmin(admin)
      optionAgente <- agenteRepo.getByIdentityAndUser(admin.identificacion, usuarioAgente)
      agente <- agenteRepo.validarUsuario(optionAgente)
      _ <- agenteRepo.validarBloqueoAdmin(agente)
      correo <- envioCorreoReinicio(agente)
    } yield correo
  }

  def cambiarEstado(admin: UsuarioAuth, usuarioAgente: String): Future[Boolean] = {
    val estadoBloqueado: Int = EstadosEmpresaEnum.bloqueadoPorAdmin.id
    for {
      _ <- validarAdmin(admin)
      optionAgente <- agenteRepo.getByIdentityAndUser(admin.identificacion, usuarioAgente)
      agente <- agenteRepo.validarUsuario(optionAgente)
      resultado <- if (agente.estado == estadoBloqueado) {
        desbloquear(agente)
      } else {
        agenteRepo.actualizarEstado(agente.id, EstadosEmpresaEnum.bloqueadoPorAdmin).map(_ > 0)
      }
    } yield resultado
  }

  def cambiarContrasena(idUsuario: Int, contrasena: String, contrasenaActual: String): Future[Int] = {
    for {
      agenteOption <- agenteRepo.getById(idUsuario)
      agente <- agenteRepo.validarUsuario(agenteOption)
      _ <- agenteRepo.validarEstado(agente)
      _ <- validarContrasena(agente, contrasenaActual)
      _ <- reglaRepo.validarContrasenaReglasGenerales(agente.id, PerfilesUsuario.agenteEmpresarial, contrasena)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), agente.id))
      actualizar <- agenteRepo.actualizarContrasena(agente.id, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasenaAgente(UltimaContrasena(None, agente.id, contrasenaHash, new Timestamp(new Date().getTime)))
    } yield actualizar
  }

  def validarContrasena(agente: UsuarioAgente, contrasena: String): Future[Boolean] = {
    val contrasenaHash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), agente.id)
    agente.contrasena.getOrElse("").equals(contrasenaHash) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.7", "No existe la contrasena"))
    }
  }

  private def desbloquear(agente: UsuarioAgente): Future[Boolean] = {
    def obtenerFecha(reglaDias: ReglaContrasena) = {
      val fechaCaducada = Calendar.getInstance()
      fechaCaducada.setTime(new Date())
      fechaCaducada.add(Calendar.DAY_OF_YEAR, reglaDias.valor.toInt * -1)
      new Timestamp(fechaCaducada.getTimeInMillis)
    }
    for {
      reglaDias <- reglaRepo.getRegla(LlavesReglaContrasena.DIAS_VALIDA)
      _ <- agenteRepo.actualizarFechaActualizacion(agente.id, obtenerFecha(reglaDias))
      correo <- envioCorreoReinicio(agente)
    } yield correo
  }

  private def envioCorreoReinicio(agente: UsuarioAgente): Future[Boolean] = {
    for {
      _ <- agenteRepo.actualizarEstado(agente.id, EstadosEmpresaEnum.pendienteReiniciarContrasena)
      expiracion <- configuracionRepo.getConfiguracion(ConfiguracionEnum.EXPIRACION_PIN)
      _ <- pinAgenteDAO.deleteAll(agente.id)
      pin <- obtenerPinAgente(agente.id, UsoPinEmpresaEnum.usoReinicioContrasena, expiracion)
      _ <- pinAgenteDAO.create(pin)
      mensaje <- obtenerMensaje(agente.correo, expiracion, pin)
      correo <- smtpRepo.enviar(mensaje)
    } yield correo
  }

  private def validarAdmin(admin: UsuarioAuth) = {
    admin.tipoCliente match {
      case TiposCliente.clienteAdministrador => Future.successful(true)
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
    val body: String = "alianza.smtp.templatepin.reiniciarContrasenaEmpresa"
    val contenido: String = new MailMessageEmpresa(body).getMessagePin(pin, expiracion.valor.toInt)
    Future.successful(Mensaje(de, para, List.empty[String], asunto, contenido))
  }

}
