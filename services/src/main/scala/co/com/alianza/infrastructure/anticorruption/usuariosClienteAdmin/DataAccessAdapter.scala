package co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin

import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarialAdmin }
import co.com.alianza.persistence.repositories.{ UsuarioEmpresarialAdminRepository }

import co.com.alianza.persistence.entities.{ UsuarioEmpresarialAdmin => eUsuarioAdmin }
import co.com.alianza.persistence.repositories.empresa.UsuariosEmpresaRepository
import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.util.clave.Crypto

/**
 * Created by josegarcia on 30/01/15.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def consultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.consultaContrasenaActual(pw_actual, idUsuario) map {
      x => transformValidation(x)
    }
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.actualizarContrasena(Crypto.hashSha512(pw_nuevo, idUsuario), idUsuario)
  }

  def existeUsuarioEmpresarialAdminActivo(nitEmpresa: String): Future[Validation[PersistenceException, Boolean]] = {
    new UsuarioEmpresarialAdminRepository().existeUsuarioEmpresarialAdminActivo(nitEmpresa)
  }

  def obtieneClientePorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] =
    new UsuariosEmpresaRepository().obtieneClientePorNitYUsuario(nit, usuario) map transformValidation

  def obtenerUsuarioEmpresarialAdminPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = {
    val repo = new UsuarioEmpresarialAdminRepository()
    repo.obtenerUsuarioEmpresarialAdminPorId(idUsuario) map transformValidation
  }

  def obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token: String): Future[Validation[PersistenceException, Option[AuditingUserData]]] = {
    val repo = new UsuarioEmpresarialAdminRepository()
    repo.obtenerUsuarioPorToken(token) map {
      x => transformValidationTuple(x)
    }
  }

  private def transformValidation(origin: Validation[PersistenceException, Option[eUsuarioAdmin]]): Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuario(usuario)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationTuple(origin: Validation[PersistenceException, Option[eUsuarioAdmin]]): Validation[PersistenceException, Option[AuditingUserData]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => {
            zSuccess(Some(AuditingUserData(usuario.tipoIdentificacion, usuario.identificacion, Some(usuario.usuario))))
          }
          case _ => zSuccess(None)
        }

      case zFailure(error) => zFailure(error)
    }
  }

}
