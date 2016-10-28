package co.com.alianza.infrastructure.anticorruption.usuarios

import java.sql.{ Date, Timestamp }

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.infrastructure.dto.{ Usuario, _ }
import co.com.alianza.persistence.entities
import co.com.alianza.persistence.entities.{ IpsUsuario, PerfilUsuario, Empresa => eEmpresa, HorarioEmpresa => eHorarioEmpresa, PinUsuario => ePinUsuario, Usuario => eUsuario, UsuarioAgente => eUsuarioEmpresarial, UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin, _ }
import co.com.alianza.persistence.repositories.{ IpsUsuarioRepository, UsuariosRepository, _ }
import co.com.alianza.persistence.util.DataBaseExecutionContext
import enumerations.EstadosUsuarioEnum

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def crearUsuario(usuario: eUsuario): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.guardar(usuario)
  }

  def relacionarIp(idUsuario: Int, ip: String): Future[Validation[PersistenceException, String]] = {
    val repo = new IpsUsuarioRepository()
    repo.guardar(IpsUsuario(idUsuario, ip))
  }

  def obtenerUsuarioNumeroIdentificacion(numeroIdentificacion: String): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioNumeroIdentificacion(numeroIdentificacion) map {
      x => transformValidation(x)
    }
  }

  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioToken(token) map {
      x => transformValidation(x)
    }
  }

  def obtenerEmpresaPorNit(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = {
    new EmpresaRepository().obtenerEmpresa(nit) map {
      x => transformValidationEmpresa(x)
    }
  }

  def obtieneUsuarioEmpresarialPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = {
    val repo = new UsuariosEmpresarialRepository()
    repo.obtieneUsuarioEmpresaPorNitYUsuario(nit, usuario) map {
      x => transformValidationUsuarioEmpresarial(x)
    }
  }

  def obtieneUsuarioEmpresarialAdminPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] =
    new UsuariosEmpresarialRepository().obtieneUsuarioEmpresaAdminPorNitYUsuario(nit, usuario) map transformValidationUsuarioEmpresarialAdmin

  def obtenerUsuarioEmpresarialToken(token: String): Future[Validation[PersistenceException, Option[(UsuarioEmpresarial, Int)]]] = {
    val repo = new UsuariosEmpresarialRepository()
    repo.obtenerUsuarioToken(token) map {
      x => transformValidationTuplaUsuarioEmpresarialEstadoEmpresa(x)
    }
  }

  def obtenerUsuarioEmpresarialAdminToken(token: String): Future[Validation[PersistenceException, Option[(UsuarioEmpresarialAdmin, Int)]]] = {
    val repo = new UsuarioEmpresarialAdminRepository()
    repo.obtenerUsuarioToken(token) map {
      x => transformValidationUsuarioEmpresarialAdminEstadoEmpresa(x)
    }
  }

  def consultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[Usuario]]] = {
    val repo = new UsuariosRepository()
    repo.consultaContrasenaActual(pw_actual, idUsuario) map {
      x => transformValidation(x)
    }
  }

  def actualizarEstadoConfronta(numeroIdentificacion: String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarEstadoUsuario(numeroIdentificacion, EstadosUsuarioEnum.activo.id)
  }

  def obtenerIpsEmpresa(idEmpresa: Int): Future[Validation[PersistenceException, Seq[IpsEmpresa]]] = {
    new IpsEmpresaRepository().obtenerIpsEmpresa(idEmpresa)
  }

  def obtenerEstadoEmpresa(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = {
    new EmpresaRepository().obtenerEmpresa(nit) map {
      x => transformValidationEmpresa(x)
    }
  }

  def actualizarEstadoUsuario(numeroIdentificacion: String, estado: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.actualizarEstadoUsuario(numeroIdentificacion, estado)
  }

  def actualizarEstadoUsuarioEmpresarialAdmin(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] =
    new UsuarioEmpresarialAdminRepository() actualizarEstadoUsuario (idUsuario, estado)

  def cambiarPassword(idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.cambiarPassword(idUsuario, password)
  }

  def asociarPerfiles(perfiles: List[PerfilUsuario]): Future[Validation[PersistenceException, List[Int]]] = {
    val repo = new UsuariosRepository()
    repo.asociarPerfiles(perfiles)
  }

  def crearUsuarioPin(pinUsuario: ePinUsuario): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosRepository()
    repo.guardarPinUsuario(pinUsuario)
  }

  def obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token: String): Future[Validation[PersistenceException, Option[AuditingUserData]]] = {
    val repo = new UsuariosRepository()
    repo.obtenerUsuarioToken(token).map(x => transformValidationTuple(x))
  }

  def crearUsuarioClienteAdministradorPin(pinUsuario: entities.PinAdmin): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuarioEmpresarialAdminRepository()
    repo.guardarPinUsuarioClienteAdmin(pinUsuario)
  }

  private def transformValidation(origin: Validation[PersistenceException, Option[eUsuario]]): Validation[PersistenceException, Option[Usuario]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) =>
            zSuccess(Some(DataAccessTranslator.translateUsuario(usuario)))
          case _ =>
            zSuccess(None)
        }
      case zFailure(error) =>
        zFailure(error)
    }
  }

  private def transformValidationEmpresa(origin: Validation[PersistenceException, Option[eEmpresa]]): Validation[PersistenceException, Option[Empresa]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(empresa) => zSuccess(Some(DataAccessTranslator.translateEmpresa(empresa)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationUsuarioEmpresarial(origin: Validation[PersistenceException, Option[eUsuarioEmpresarial]]): Validation[PersistenceException, Option[UsuarioEmpresarial]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuarioEmpresarial(usuario)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  /**
   * Devuelvo la tupla del usuario empresarial y el estado de la empresa, ya tranformando la entidad de persistencia UsuarioEmpresarial en el objeto DTO
   */
  private def transformValidationTuplaUsuarioEmpresarialEstadoEmpresa(origin: Validation[PersistenceException, Option[(eUsuarioEmpresarial, Int)]]): Validation[PersistenceException, Option[(UsuarioEmpresarial, Int)]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateTuplaUsuarioEmpresarialEstadoEmpresa(usuario)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationUsuarioEmpresarialAdmin(origin: Validation[PersistenceException, Option[eUsuarioEmpresarialAdmin]]): Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]] =
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuarioEmpresarialAdmin(usuario)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }

  /**
   * Devuelvo la tupla del usuario empresarial Admin y el estado de la empresa, ya tranformando la entidad de persistencia UsuarioEmpresarialAdmin en el objeto DTO
   */
  private def transformValidationUsuarioEmpresarialAdminEstadoEmpresa(origin: Validation[PersistenceException, Option[(eUsuarioEmpresarialAdmin, Int)]]): Validation[PersistenceException, Option[(UsuarioEmpresarialAdmin, Int)]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuarioEmpresarialAdminEstadoEmpresa(usuario)))
          case _ => zSuccess(None)
        }
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationTuple(origin: Validation[PersistenceException, Option[eUsuario]]): Validation[PersistenceException, Option[AuditingUserData]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => {
            val user = DataAccessTranslator.translateUsuario(usuario)
            zSuccess(Some(AuditingUserData(user.tipoIdentificacion, user.identificacion, None)))
          }
          case _ => zSuccess(None)
        }

      case zFailure(error) => zFailure(error)
    }
  }

}