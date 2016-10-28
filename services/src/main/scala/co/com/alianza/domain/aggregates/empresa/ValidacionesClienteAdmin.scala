package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.domain.aggregates.usuarios._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => UsDataAdapter }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => DataAccessAdapterClienteAdmin }
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarialAdmin, _ }
import co.com.alianza.infrastructure.messages.ErrorMessage
import co.com.alianza.persistence.util.DataBaseExecutionContext
import co.com.alianza.util.clave.{ Crypto, ValidarClave }
import enumerations.empresa.EstadosDeEmpresaEnum
import enumerations.{ EstadosEmpresaEnum, PerfilesUsuario }
import portal.transaccional.autenticacion.service.drivers.reglas.ValidacionClave

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation.FlatMap._
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by josegarcia on 30/01/15.
 */
object ValidacionesClienteAdmin {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext
  import co.com.alianza.util.json.MarshallableImplicits._

  def validacionConsultaContrasenaActualClienteAdmin(pw_actual: String, idUsuario: Int): Future[Validation[ErrorValidacion, Option[UsuarioEmpresarialAdmin]]] = {
    val contrasenaActualFuture = DataAccessAdapterClienteAdmin.consultaContrasenaActual(Crypto.hashSha512(pw_actual, idUsuario), idUsuario)
    contrasenaActualFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[UsuarioEmpresarialAdmin]) =>
        x match {
          case Some(c) =>
            zSuccess(x)
          case None =>
            zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
          case _ =>
            zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoContempla))
        }
    })
  }

  def validacionReglasClave(contrasena: String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario): Future[Validation[ErrorValidacion, Unit.type]] = {
    val usuarioFuture: Future[Validation[PersistenceException, List[ValidacionClave]]] = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: List[ValidacionClave]) =>
        x match {
          case List() => zSuccess(Unit)
          case erroresList =>
            val errores = erroresList.foldLeft("")((z, i) => i.toString + "-" + z)
            zFailure(ErrorFormatoClave(errorClave(errores)))
        }
    })
  }

  private def errorClave(error: String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  private val errorContrasenaActualNoContempla = ErrorMessage("409.8", "No comtempla la contrasena actual", "No comtempla la contrasena actual").toJson

}
