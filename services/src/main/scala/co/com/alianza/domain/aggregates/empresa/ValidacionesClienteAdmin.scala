package co.com.alianza.domain.aggregates.empresa

import enumerations.empresa.EstadosDeEmpresaEnum

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure, Success, Validation}
import co.com.alianza.domain.aggregates.usuarios._
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{DataAccessAdapter => DataAccessAdapterClienteAdmin}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.dto._
import co.com.alianza.util.clave.{ValidarClave, ErrorValidacionClave, Crypto}
import co.com.alianza.app.MainActors
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import com.typesafe.config.Config
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.infrastructure.messages.ErrorMessage
import enumerations.PerfilesUsuario
import co.com.alianza.exceptions.PersistenceException

/**
 * Created by josegarcia on 30/01/15.
 */
object ValidacionesClienteAdmin {

  import co.com.alianza.util.json.MarshallableImplicits._
  implicit val _: ExecutionContext = MainActors.dataAccesEx
  implicit private val config: Config = MainActors.conf


  def validacionConsultaContrasenaActualClienteAdmin(pw_actual: String, idUsuario: Int): Future[Validation[ErrorValidacion, Option[UsuarioEmpresarialAdmin]]] = {
    val contrasenaActualFuture = DataAccessAdapterClienteAdmin.consultaContrasenaActual(Crypto.hashSha512(pw_actual), idUsuario)
    contrasenaActualFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[UsuarioEmpresarialAdmin]) => x match{
        case Some(c) =>
          zSuccess(x)
        case None =>
          zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
        case _ =>
          zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoContempla))
      }
    })
  }

  def validacionReglasClave(contrasena:String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario): Future[Validation[ErrorValidacion, Unit.type]] = {

    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*)

    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:List[ErrorValidacionClave]) => x match{
        case List() => zSuccess(Unit)
        case erroresList =>
          val errores = erroresList.foldLeft("") ( (z, i) => i.toString + "-" + z  )
          zFailure(ErrorFormatoClave(errorClave(errores)))
      }
    })
  }

  def validacionObtenerClienteAdminPorId( idUsuario: Int ) : Future[Validation[ErrorValidacion, UsuarioEmpresarialAdmin]] = {

    val clienteAdminFuture: Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = DataAccessAdapterClienteAdmin.obtenerUsuarioEmpresarialAdminPorId(idUsuario)
    clienteAdminFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (clienteAdmin: Option[UsuarioEmpresarialAdmin]) => clienteAdmin match{
        case None => zFailure(ErrorUsuarioNoExiste(errorUsuarioNoExiste))
        case Some(clienteAdmin) => zSuccess(clienteAdmin)
      }
    })

  }

  def validarEstadoEmpresa(nit: String): Future[Validation[ErrorValidacion, Boolean]] = {
    val empresaActiva : Int = EstadosDeEmpresaEnum.activa.id
    val estadoEmpresaFuture: Future[Validation[PersistenceException, Option[Empresa]]] = UsDataAdapter.obtenerEstadoEmpresa(nit)
    estadoEmpresaFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      case Some(empresa) =>
        empresa.estado match {
          case `empresaActiva` => Validation.success(true)
          case _ => Validation.failure(ErrorEmpresaAccesoDenegado(errorEmpresaAccesoDenegado))
        }
      case None => Validation.failure(ErrorClienteNoExiste(errorClienteNoExiste ))
    })
  }

  private def errorClave(error:String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  private val errorContrasenaActualNoContempla = ErrorMessage("409.8", "No comtempla la contrasena actual", "No comtempla la contrasena actual").toJson
  private val errorUsuarioNoExiste = ErrorMessage("409.9", "El usuario no existe", "El usuario no existe - validacion cliente admin").toJson
  private val errorClienteNoExiste = ErrorMessage("409.10", "El Cliente no existe en core", "El Cliente no existe en core - validacion cliente admin").toJson
  private val errorEmpresaAccesoDenegado = ErrorMessage("409.11", "La empresa tiene el acceso desactivado", "La empresa tiene el acceso desactivado - validacion cliente admin").toJson

}
