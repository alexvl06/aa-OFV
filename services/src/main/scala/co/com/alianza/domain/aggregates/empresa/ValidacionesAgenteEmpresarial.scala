package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.usuarios._
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => DataAccessAdapterUsuarioAE }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => CliAdmDataAccessAdapter }
import co.com.alianza.infrastructure.dto.{ Configuracion, Empresa, UsuarioEmpresarial, UsuarioEmpresarialAdmin }
import co.com.alianza.infrastructure.messages.ErrorMessage
import enumerations.empresa.EstadosDeEmpresaEnum
import enumerations.{ EstadosEmpresaEnum, PerfilesUsuario }
import co.com.alianza.infrastructure.anticorruption.configuraciones.{ DataAccessAdapter => dataAccesAdaptarConf }
import portal.transaccional.autenticacion.service.drivers.reglas.ValidacionClave

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.util.clave.{ Crypto, ValidarClave }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => UsDataAdapter }
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scalaz.Validation.FlatMap._

/**
 * Created by S4N on 16/12/14.
 */
object ValidacionesAgenteEmpresarial {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  import co.com.alianza.util.json.MarshallableImplicits._

  def validarEstadoEmpresa(nit: String): Future[Validation[ErrorValidacion, Boolean]] = {
    val empresaActiva: Int = EstadosDeEmpresaEnum.activa.id
    val estadoEmpresaFuture: Future[Validation[PersistenceException, Option[Empresa]]] = UsDataAdapter.obtenerEstadoEmpresa(nit)
    estadoEmpresaFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      case Some(empresa) =>
        empresa.estado match {
          case `empresaActiva` => Validation.success(true)
          case _ => Validation.failure(ErrorEmpresaAccesoDenegado(errorEmpresaAccesoDenegado))
        }
      case None => Validation.failure(ErrorClienteNoExiste(errorClienteNoExiste))
    })
  }

  /**
   * Valida si el cliente ya se encuentra registrado un cliente administrador con el mismo usuario
   */
  def validarUsuarioClienteAdmin(nit: String, usuario: String): Future[Validation[ErrorValidacion, Boolean]] =
    CliAdmDataAccessAdapter obtieneClientePorNitYUsuario (nit, usuario) map {
      _ leftMap { e => ErrorPersistence(e.message, e) } flatMap {
        u: Option[UsuarioEmpresarialAdmin] =>
          u match {
            case None => zSuccess(true)
            case Some(_) => zFailure(ErrorUsuarioClienteAdmin(errorUsuarioClienteAdmin))
          }
      }
    }

  /**
   * Validar usuario
   * @param idUsuario
   * @param usuario
   * @return
   */
  def validarUsuarioAgente(idUsuario: Int, nit: String, usuario: String): Future[Validation[ErrorValidacion, Boolean]] =
    DataAccessAdapterUsuarioAE existeUsuarioEmpresarialPorUsuario (idUsuario: Int, nit, usuario) map {
      _ leftMap { e => ErrorPersistence(e.message, e) } flatMap {
        existe: Boolean =>
          existe match {
            case false => zSuccess(true)
            case true => zFailure(ErrorUsuarioClienteAdmin(errorUsuarioClienteAdmin))
          }
      }
    }

  def validacionEstadoActualizacionAgenteEmpresarial(idAgenteEmpresarial: Int): Future[Validation[ErrorValidacion, Boolean]] = {
    val usuarioAgenteEmpresarialFuture = DataAccessAdapterUsuarioAE.obtenerUsuarioEmpresarialPorId(idAgenteEmpresarial)
    usuarioAgenteEmpresarialFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (idUsuarioAgenteEmpresarial: Option[UsuarioEmpresarial]) =>
        idUsuarioAgenteEmpresarial match {
          //TODO: Cambiar por enums correspondiente
          case Some(x) => x.estado match {
            case 0 => zFailure(ErrorEstadoActualizarAgenteEmpresarial(errorEstadoAgenteEmpresarial))
            case 4 => zFailure(ErrorEstadoActualizarAgenteEmpresarial(errorEstadoAgenteEmpresarial))
            case _ => zSuccess(true)
          }
          case None => zFailure(ErrorAgenteEmpresarialNoExiste(errorAgenteEmpresarialNoExiste))
        }
    })
  }

  //Los mensajes de error en empresa se relacionaran como 01-02-03 << Ejemplo: 409.01 >>
  private val errorAgenteEmpresarialNoExiste = ErrorMessage("409.01", "No existe el Agente Empresarial", "No existe el Agente Empresarial").toJson
  private val errorEstadoAgenteEmpresarial = ErrorMessage("409.02", "El estado actual del usuario no permite el reinicio de contrasena", "El estado actual del usuario no permite el reinicio de contrasena").toJson
  private val errorUsuarioClienteAdmin = ErrorMessage("409.9", "El usuario ya está registrado en esta empresa.", "El usuario ya está registrado en esta empresa.").toJson
  private val errorClienteNoExiste = ErrorMessage("409.10", "El Cliente no existe en core", "El Cliente no existe en core").toJson
  private val errorEmpresaAccesoDenegado = ErrorMessage("409.11", "La empresa tiene el acceso desactivado", "La empresa tiene el acceso desactivado").toJson

}
