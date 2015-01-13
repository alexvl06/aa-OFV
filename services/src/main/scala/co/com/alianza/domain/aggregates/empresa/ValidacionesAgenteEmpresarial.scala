package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.empresa.ErrorValidacionEmpresa
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion}
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterUsuarioAE}
import co.com.alianza.infrastructure.dto.Configuracion
import co.com.alianza.infrastructure.messages.ErrorMessage
import enumerations.EstadosEmpresaEnum
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessTranslator => dataAccessTransConf, DataAccessAdapter => dataAccesAdaptarConf}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

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

  //Los mensajes de error en empresa se relacionaran como 01-02-03 << Ejemplo: 409.01 >>
  private val errorAgenteEmpresarialNoExiste = ErrorMessage("409.01", "No existe el Agente Empresarial", "No existe el Agente Empresarial").toJson
  private val errorEstadoAgenteEmpresarial = ErrorMessage("409.02", "El estado actual del usuario no permite el reinicio de contrasena", "El estado actual del usuario no permite el reinicio de contrasena").toJson

}
