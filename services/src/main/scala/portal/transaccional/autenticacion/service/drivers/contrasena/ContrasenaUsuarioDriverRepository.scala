package portal.transaccional.autenticacion.service.drivers.contrasena

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.persistence.entities.{UltimaContrasena, Usuario}
import co.com.alianza.util.clave.Crypto
import enumerations.{AppendPasswordUser, PerfilesUsuario}
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import scala.concurrent.Future

/**
 * Created by hernando on 9/11/16.
 */
case class ContrasenaUsuarioDriverRepository(ultimaContrasenaRepo: UltimaContrasenaRepository,
                                             usuarioRepo: UsuarioRepository, reglaRepo: ReglaContrasenaRepository) (implicit val ex: ExecutionContext) extends ContrasenaUsuarioRepository {

  def cambiarContrasena(idUsuario: Int, contrasena: String, contrasenaActual: String): Future[Int] = {
    for {
      usuarioOption <- usuarioRepo.getById(idUsuario)
      usuario <- usuarioRepo.validarUsuario(usuarioOption)
      _ <- usuarioRepo.validarEstado(usuario)
      _ <- validarContrasena(usuario, contrasenaActual)
      _ <- reglaRepo.validarContrasenaReglasGenerales(idUsuario, PerfilesUsuario.clienteIndividual, contrasena)
      contrasenaHash <- Future.successful(Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario))
      actualizar <- usuarioRepo.actualizarContrasena(idUsuario, contrasenaHash)
      _ <- ultimaContrasenaRepo.crearUltimaContrasenaAgente(UltimaContrasena(None, idUsuario, contrasenaHash, new Timestamp(new Date().getTime)))
    } yield actualizar
  }

  def validarContrasena(usuario: Usuario, contrasena: String): Future[Boolean] = {
    val contrasenaHash = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), usuario.id)
    usuario.contrasena.equals(contrasenaHash) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.7", "No existe la contrasena"))
    }
  }
}
