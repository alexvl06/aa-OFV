package portal.transaccional.autenticacion.service.drivers.usuarioAgenteEmpresarial

import java.sql.Timestamp
import java.util.Date

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{UsuarioAgente, UsuarioEmpresarial, UsuarioEmpresarialTable}
import enumerations.EstadosEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.{UsuarioEmpresarialRepository, UsuarioEmpresarialRepositoryG}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialDAO

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by alexandra on 2016
 */
case class UsuarioEmpresarialDriverRepository(usuarioDAO: UsuarioEmpresarialDAO)(implicit val ex: ExecutionContext)
  extends UsuarioEmpresarialRepositoryG[UsuarioEmpresarialTable, UsuarioEmpresarial](usuarioDAO)
  with UsuarioEmpresarialRepository[UsuarioEmpresarial] with UsuarioEmpresarialCustomRepository {

  /**
    * Guardar contrasena
    *
    * @param idUsuario
    * @param contrasena
    */
  def actualizarContrasena(idUsuario: Int, contrasena: String): Future[Int] = {
    for {
      cambiar <- usuarioDAO.updatePasswordById(idUsuario, contrasena)
      _ <- usuarioDAO.updateStateById(idUsuario, EstadosEmpresaEnum.activo.id)
      _ <- usuarioDAO.updateUpdateDate(idUsuario, new Timestamp(new Date().getTime))
      _ <- usuarioDAO.updateIncorrectEntries(idUsuario, 0)
    } yield cambiar
  }



  def validacionBloqueoAdmin(usuario: UsuarioAgente): Future[Boolean] = {
    val bloqueoPorAdmin: Int = EstadosEmpresaEnum.bloqueadoPorAdmin.id
    usuario.estado match {
      case `bloqueoPorAdmin` => Future.failed(ValidacionException("409.13", "Usuario Bloqueado Admin"))
      case _ => Future.successful(true)
    }
  }

  def validarUsuario(usuarioOption: Option[UsuarioAgente]): Future[UsuarioAgente] = {
    usuarioOption match {
      case Some(usuario) => Future.successful(usuario)
      case _ => Future.failed(ValidacionException("409.01", "No existe usuario"))
    }
  }

}
