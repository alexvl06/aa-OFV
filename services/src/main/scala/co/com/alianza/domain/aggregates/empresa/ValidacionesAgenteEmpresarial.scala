package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.empresa.ErrorValidacionEmpresa
import co.com.alianza.domain.aggregates.usuarios.{ErrorFormatoClave, ErrorContrasenaNoExiste, ErrorPersistence, ErrorValidacion}
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterUsuarioAE}
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial, UsuarioEmpresarialAdmin, Configuracion}
import co.com.alianza.infrastructure.messages.ErrorMessage
import enumerations.{PerfilesUsuario, EstadosEmpresaEnum}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessTranslator => dataAccessTransConf, DataAccessAdapter => dataAccesAdaptarConf}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.DataAccessAdapter
import co.com.alianza.util.clave.{ValidarClave, ErrorValidacionClave, Crypto}
import co.com.alianza.exceptions.PersistenceException

/**
 * Created by S4N on 16/12/14.
 */
object ValidacionesAgenteEmpresarial {

  import co.com.alianza.util.json.MarshallableImplicits._
  implicit val _: ExecutionContext = MainActors.dataAccesEx

  /*
  Este Metodo de validacionAgenteEmpresarial Me retorna el id de este usuario si cumple con los 3 parametros que se le envian a la DB
   */
  def validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[ErrorValidacionEmpresa, Int]] = {
    val usuarioAgenteEmpresarialFuture = DataAccessAdapterUsuarioAE.validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int)
    usuarioAgenteEmpresarialFuture.map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message,pe)).flatMap{
      (idUsuarioAgenteEmpresarial: Option[(Int,Int)]) => idUsuarioAgenteEmpresarial match{
        case Some(x) => x._2 match {
          case 1 => zSuccess(x._1)
          case 3 => zSuccess(x._1)
          case _ => zFailure(ErrorEstadoAgenteEmpresarial(errorEstadoAgenteEmpresarial))
        }
        case None => zFailure(ErrorAgenteEmpresarialNoExiste(errorAgenteEmpresarialNoExiste))
      }
    })
  }

  def validacionConsultaTiempoExpiracion(): Future[Validation[ErrorValidacionEmpresa, Configuracion]] = {
    val configuracionFuture = dataAccesAdaptarConf.obtenerConfiguracionPorLlave( TiposConfiguracion.EXPIRACION_PIN.llave )
    configuracionFuture.map(_.leftMap(pe => ErrorPersistenceEmpresa(pe.message,pe)).flatMap{
      (x:Option[Configuracion]) => x match{
        case Some(c) => zSuccess(c)
      }
    })
  }

  def validacionConsultaContrasenaActualAgenteEmpresarial(pw_actual: String, idUsuario: Int): Future[Validation[ErrorValidacion, Option[UsuarioEmpresarial]]] = {
    val contrasenaActualFuture = DataAccessAdapterUsuarioAE.consultaContrasenaActualAgenteEmpresarial(Crypto.hashSha512(pw_actual), idUsuario)
    contrasenaActualFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[UsuarioEmpresarial]) => x match{
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


  //Los mensajes de error en empresa se relacionaran como 01-02-03 << Ejemplo: 409.01 >>
  private val errorAgenteEmpresarialNoExiste = ErrorMessage("409.01", "No existe el Agente Empresarial", "No existe el Agente Empresarial").toJson
  private val errorEstadoAgenteEmpresarial = ErrorMessage("409.02", "El estado actual del usuario no permite el reinicio de contrasena", "El estado actual del usuario no permite el reinicio de contrasena").toJson
  private def errorClave(error:String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  private val errorContrasenaActualNoContempla = ErrorMessage("409.8", "No comtempla la contrasena actual", "No comtempla la contrasena actual").toJson

}
