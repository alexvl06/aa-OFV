package portal.transaccional.autenticacion.service.drivers.contrasena

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ UltimaContrasena, UsuarioEmpresarialAdmin }
import co.com.alianza.util.clave.Crypto
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 22/11/16.
 */
case class ContrasenaAdminDriverRepository(ultimaContrasenaRepo: UltimaContrasenaRepository, adminRepo: UsuarioAdminRepository,
    reglaRepo: ReglaContrasenaRepository)(implicit val ex: ExecutionContext) extends ContrasenaAdminRepository {

  def cambiarContrasena(idUsuario: Int, contrasena: String, contrasenaActual: String): Future[Int] = {
    //val tk_validation = Token.autorizarToken(message.token)
    for {
      admin <- adminRepo.getById(idUsuario)
      _ <- adminRepo.validarEstado(admin)
      _ <- validarContrasena(admin, contrasenaActual)
      _ <- reglaRepo.validarContrasenaReglasGenerales(admin.id, PerfilesUsuario.clienteAdministrador, contrasena)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), admin.id))
      actualizar <- adminRepo.actualizarContrasena(admin.id, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasenaAdmin(UltimaContrasena(None, admin.id, contrasenaHash, new Timestamp(new Date().getTime)))
    } yield actualizar
  }

  def validarContrasena(admin: UsuarioEmpresarialAdmin, contrasena: String): Future[Boolean] = {
    val contrasenaHash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), admin.id)
    admin.contrasena.equals(contrasenaHash) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.7", "No existe la contrasena"))
    }
  }

}
