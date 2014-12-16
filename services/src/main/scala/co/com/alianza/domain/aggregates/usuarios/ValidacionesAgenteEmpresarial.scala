package co.com.alianza.domain.aggregates.usuarios

import scala.concurrent.Future
import scalaz.Validation
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterUsuarioAE }

/**
 * Created by S4N on 16/12/14.
 */
object ValidacionesAgenteEmpresarial {

  def validacionAgenteEmpresarial(): Future[Validation[ErrorValidacion, Int]] = {
    val usuarioFuture = DataAccessAdapterUsuarioAE.obtenerUsuarioNumeroIdentificacion(message.identificacion)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[Usuario]) => x match{
        case None => zSuccess(Unit)
        case _ => zFailure(ErrorDocumentoExiste(errorUsuarioExiste))
      }
    })
  }

}
