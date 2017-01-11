package portal.transaccional.autenticacion.service.drivers.pin

import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities._
import co.com.alianza.util.clave.Crypto
import enumerations._
import portal.transaccional.autenticacion.service.drivers.empresa.EmpresaRepository
import portal.transaccional.autenticacion.service.drivers.ipempresa.IpEmpresaRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepositoryG
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{PinAdminDAOs, PinAgenteDAOs, PinUsuarioDAOs}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by hernando on 25/10/16.
 */
case class PinDriverRepository(pinUsuarioDAO: PinUsuarioDAOs, pinAdminDAO: PinAdminDAOs, pinAgenteDAO: PinAgenteDAOs,
    empresaRepo: EmpresaRepository, ipUsuarioRepo: IpUsuarioRepository, ipEmpresaRepo: IpEmpresaRepository,
    usuarioRepo: UsuarioRepository, usuarioAdminRepo: UsuarioAdminRepository, usuarioAgenteRepo: UsuarioEmpresarialRepositoryG[UsuarioEmpresarialTable, UsuarioEmpresarial],
    ultimaContrasenaRepo: UltimaContrasenaRepository, reglasRepo: ReglaContrasenaRepository)(implicit val ex: ExecutionContext) extends PinRepository {

  def validarPinUsuario(token: String, funcionalidad: Int): Future[Boolean] = {
    for {
      pinOption <- pinUsuarioDAO.findById(token)
      pin <- validarPinOption[PinUsuario](pinOption)
      validar <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      _ <- if (funcionalidad == 1) usuarioRepo.actualizarEstado(pin.idUsuario, EstadosUsuarioEnum.pendienteActivacion.id) else Future.successful(true)
    } yield validar
  }

  def validarPinAdmin(token: String, funcionalidad: Int): Future[Boolean] = {
    for {
      pinOption <- pinAdminDAO.findById(token)
      pin <- validarPinOption(pinOption)
      validar <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      admin <- usuarioAdminRepo.getById(pin.idUsuario)
      optionEmpresa <- empresaRepo.getByIdentity(admin.identificacion)
      _ <- empresaRepo.validarEmpresa(optionEmpresa)
      _ <- usuarioAdminRepo.validacionBloqueoAdmin(admin)
      _ <- usuarioAdminRepo.validacionExisteAdminActivo(admin)
      _ <- if (funcionalidad == 1) usuarioAdminRepo.actualizarEstado(admin.id, EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
      else Future.successful(true)
    } yield validar
  }

  def validarPinAgente(token: String): Future[Boolean] = {
    for {
      pinOption <- pinAgenteDAO.findById(token)
      pin <- validarPinOption(pinOption)
      validar <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      agenteOption <- usuarioAgenteRepo.getById(pin.idUsuarioEmpresarial)
      agente <- usuarioAgenteRepo.validarUsuario(agenteOption)
      _ <- usuarioAgenteRepo.validarBloqueoAdmin(agente)
      optionEmpresa <- empresaRepo.getByIdentity(agente.identificacion)
      _ <- empresaRepo.validarEmpresa(optionEmpresa)
    } yield validar
  }

  def cambioContrasenaUsuario(token: String, contrasena: String, ip: Option[String]): Future[Int] = {
    for {
      pinOption <- pinUsuarioDAO.findById(token)
      pin <- validarPinOption(pinOption)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), pin.idUsuario))
      _ <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      _ <- reglasRepo.validarContrasenaReglasGenerales(pin.idUsuario, PerfilesUsuario.clienteIndividual, contrasena)
      - <- usuarioRepo.actualizarContrasena(pin.idUsuario, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasena(UltimaContrasena(None, pin.idUsuario, contrasenaHash, new Timestamp(new Date().getTime)))
      _ <- guardarIp(pin.idUsuario, ip)
      eliminar <- pinUsuarioDAO.delete(token)
    } yield eliminar
  }

  def cambioContrasenaAdmin(token: String, contrasena: String, ip: Option[String]): Future[Int] = {
    for {
      pinOption <- pinAdminDAO.findById(token)
      pin <- validarPinOption(pinOption)
      _ <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      admin <- usuarioAdminRepo.getById(pin.idUsuario)
      _ <- usuarioAdminRepo.validacionBloqueoAdmin(admin)
      optionEmpresa <- empresaRepo.getByIdentity(admin.identificacion)
      _ <- empresaRepo.validarEmpresa(optionEmpresa)
      _ <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      _ <- reglasRepo.validarContrasenaReglasGenerales(pin.idUsuario, PerfilesUsuario.clienteAdministrador, contrasena)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), pin.idUsuario))
      _ <- usuarioAdminRepo.actualizarContrasena(admin.id, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasenaAdmin(UltimaContrasena(None, pin.idUsuario, contrasenaHash, new Timestamp(new Date().getTime)))
      _ <- guardarIpEmpresa(optionEmpresa.get.id, ip)
      eliminar <- pinAdminDAO.delete(token)
    } yield eliminar
  }

  def cambioContrasenaAgente(token: String, contrasena: String): Future[Int] = {
    for {
      pinOption <- pinAgenteDAO.findById(token)
      pin <- validarPinOption(pinOption)
      validar <- validar(token, pin.token, pin.tokenHash, new Date(pin.fechaExpiracion.getTime))
      agenteOption <- usuarioAgenteRepo.getById(pin.idUsuarioEmpresarial)
      agente <- usuarioAgenteRepo.validarUsuario(agenteOption)
      _ <- usuarioAgenteRepo.validarBloqueoAdmin(agente)
      optionEmpresa <- empresaRepo.getByIdentity(agente.identificacion)
      _ <- empresaRepo.validarEmpresa(optionEmpresa)
      _ <- reglasRepo.validarContrasenaReglasGenerales(pin.idUsuarioEmpresarial, PerfilesUsuario.agenteEmpresarial, contrasena)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), pin.idUsuarioEmpresarial))
      _ <- usuarioAgenteRepo.actualizarContrasena(agente.id, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasenaAgente(UltimaContrasena(None, agente.id, contrasenaHash, new Timestamp(new Date().getTime)))
      eliminar <- pinAgenteDAO.delete(token)
    } yield eliminar
  }

  private def guardarIp(idUsuario: Int, ipOption: Option[String]): Future[String] = {
    ipOption match {
      case Some(ip: String) => ipUsuarioRepo.guardarIp(IpsUsuario(idUsuario, ip))
      case _ => Future.successful("")
    }
  }

  private def guardarIpEmpresa(idEmpresa: Int, ipOption: Option[String]): Future[String] = {
    ipOption match {
      case Some(ip: String) =>
        for {
          ips <- ipEmpresaRepo.getIpsByEmpresaId(idEmpresa)
          guardar <- if (ips.exists(_.ip.equals(ip))) {
            Future.successful("")
          } else {
            ipEmpresaRepo.guardarIp(IpsEmpresa(idEmpresa, ip))
          }
        } yield guardar
      case _ => Future.successful("")
    }
  }

  private def validarPinOption[T](pinOption: Option[T]): Future[T] = {
    pinOption match {
      case Some(pin) => Future.successful(pin)
      case _ => Future.failed(ValidacionException("409.1", "Pin invalido"))
    }
  }

  def validar(pin: String, token: String, tokenHash: String, fecha: Date): Future[Boolean] = {
    val pinHash: String = deserializarPin(token, fecha)
    if (pinHash.equals(tokenHash) && new Date().getTime < fecha.getTime) Future.successful(true)
    else Future.failed(ValidacionException("409.1", "Pin invalido"))
  }

  private def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    //formatear fecha
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val fechaFormato: String = format.format(fechaExpiracion)
    //digest
    val hash: Array[Byte] = md.digest(s"""${pin} - ${fechaFormato}""".getBytes)
    val hexString: StringBuffer = new StringBuffer()
    for (i <- hash) hexString.append(Integer.toHexString(0xFF & i))
    hexString.toString
  }

}
