package co.com.alianza.domain.aggregates.empresa

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure, Success, Validation}
import co.com.alianza.domain.aggregates.usuarios.{ErrorFormatoClave, ErrorContrasenaNoExiste, ErrorPersistence, ErrorValidacion}
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{DataAccessAdapter => DataAccessAdapterClienteAdmin}
import co.com.alianza.infrastructure.dto.Usuario
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
          println("S---"+x)
          zSuccess(x)
        case None =>
          println("N---"+x)
          zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
        case _ =>
          println("_---"+x)
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

  private def errorClave(error:String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  private val errorContrasenaActualNoContempla = ErrorMessage("409.8", "No comtempla la contrasena actual", "No comtempla la contrasena actual").toJson


}
