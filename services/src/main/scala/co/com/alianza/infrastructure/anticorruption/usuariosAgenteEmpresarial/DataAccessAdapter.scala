package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.infrastructure.auditing.AuditingUser.AuditingUserData
import co.com.alianza.persistence.entities.{ PinAgente => ePinEmpresa, UsuarioAgenteEmpresarial => eUsuario, _ }
import co.com.alianza.persistence.repositories.UsuariosEmpresarialRepository
import co.com.alianza.exceptions.PersistenceException
import enumerations.EstadosEmpresaEnum

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Failure, Success, Validation }
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarial => dtoUsuario, UsuarioEmpresarialEstado => dtoEstadoUsuario }
import co.com.alianza.persistence.repositories.empresa.UsuariosEmpresaRepository

import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.util.clave.Crypto
import co.com.alianza.persistence.entities.IpsUsuario
import co.com.alianza.persistence.entities.Empresa
import co.com.alianza.persistence.messages.empresa.GetAgentesEmpresarialesRequest
import co.com.alianza.persistence.entities.UsuarioEmpresarialEmpresa
import co.com.alianza.persistence.util.DataBaseExecutionContext

/**
 * Created by S4N on 16/12/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  val repo = new UsuariosEmpresarialRepository()

  def consultaContrasenaActualAgenteEmpresarial(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[dtoUsuario]]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.consultaContrasenaActualAgenteEmpresarial(pw_actual, idUsuario) map {
      x => transformValidation(x)
    }
  }

  def consultaAgenteEmpresarial(nit: String, usuario: String): Future[Validation[PersistenceException, Option[eUsuario]]] = {
    repo.obtieneUsuarioEmpresaPorNitYUsuario(nit, usuario)
  }

  def validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, Option[(Int, Int)]]] = {
    repo.validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int)
  }

  def actualizarContrasenaAgenteEmpresarial(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.actualizarContrasenaAgenteEmpresarial(Crypto.hashSha512(pw_nuevo, idUsuario), idUsuario)
  }

  def caducarFechaUltimoCambioContrasenaAgenteEmpresarial(idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.caducarFechaUltimoCambioContrasenaAgenteEmpresarial(idUsuario)
  }

  def cambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.AcambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado)
  }

  def cambiarBloqueoDesbloqueoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado: EstadosEmpresaEnum.estadoEmpresa, timestamp: Timestamp): Future[Validation[PersistenceException, Int]] = {
    repo.cambiarBloqueoDesbloqueoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado, timestamp)
  }

  def crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial: ePinEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.guardarPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)
  }

  def crearAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial: eUsuario): Future[Validation[PersistenceException, Int]] = {
    repo.insertarAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial)
  }

  def actualizarAgenteEmpresarial(id: Int, usuario: String, correo: String, nombreUsuario: String, cargo: String, descripcion: String): Future[Validation[PersistenceException, Int]] = {
    repo.actualizarAgente(id, usuario, correo, nombreUsuario, cargo, descripcion)
  }

  def eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial: Int, usoPinEmpresa: Int): Future[Validation[PersistenceException, Int]] = {
    repo.eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial, usoPinEmpresa)
  }

  def obtenerEmpresaPorNit(nit: String): Future[Validation[PersistenceException, Option[Empresa]]] = {
    repo.obtenerEmpresaPorNit(nit)
  }

  def asociarAgenteConEmpresa(usuarioEmpresarialEmpresa: UsuarioEmpresarialEmpresa) = {
    repo.asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa)
  }

  def obtenerUsuarioEmpresarialPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[dtoUsuario]]] = {
    repo.obtenerUsuarioEmpresarialPorId(idUsuario) map transformValidation
  }

  def existeUsuarioEmpresarialPorUsuario(idUsuario: Int, nit: String, usuario: String): Future[Validation[PersistenceException, Boolean]] = {
    repo.existeUsuario(idUsuario, nit, usuario)
  }

  def obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token: String): Future[Validation[PersistenceException, Option[AuditingUserData]]] = {
    repo.obtenerUsuarioPorToken(token) map {
      x => transformValidationTuple(x)
    }
  }

  def obtenerUsuariosBusqueda(message: GetAgentesEmpresarialesRequest): Future[Validation[PersistenceException, List[dtoEstadoUsuario]]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.obtenerUsuariosBusqueda(message.correo, message.usuario, message.nombre, message.estado, message.idClienteAdmin) map {
      x => transformValidationList(x)
    }
  }

  def cambiarPasswordUsuarioAgenteEmpresarial(idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] =
    new UsuariosEmpresaRepository() cambiarPassword (idUsuario, password)

  def actualizarEstadoUsuarioAgenteEmpresarial(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.actualizarEstadoUsuario(idUsuario, estado)
  }

  def asociarPerfiles(idAgente: Int, idsPerfiles: List[Int]): Future[Validation[PersistenceException, List[Int]]] =
    new UsuariosEmpresaRepository() asociarPerfiles (idsPerfiles map { PerfilAgenteAgente(idAgente, _) })

  private def transformValidation(origin: Validation[PersistenceException, Option[eUsuario]]): Validation[PersistenceException, Option[dtoUsuario]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => zSuccess(Some(DataAccessTranslator.translateUsuarioEmpresarial(usuario)))
          case _ => zSuccess(None)
        }

      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationList(origin: Validation[PersistenceException, Seq[eUsuario]]): Validation[PersistenceException, List[dtoEstadoUsuario]] = {
    origin match {
      case zSuccess(response: Seq[eUsuario]) => zSuccess(DataAccessTranslator.translateUsuarioEstado(response))
      case zFailure(error) => zFailure(error)
    }
  }

  private def transformValidationTuple(origin: Validation[PersistenceException, Option[eUsuario]]): Validation[PersistenceException, Option[AuditingUserData]] = {
    origin match {
      case zSuccess(response) =>
        response match {
          case Some(usuario) => {
            zSuccess(Some(AuditingUserData(usuario.tipoIdentificacion, usuario.identificacion, Some(usuario.nombreUsuario))))
          }
          case _ => zSuccess(None)
        }

      case zFailure(error) => zFailure(error)
    }
  }

}
