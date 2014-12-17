package co.com.alianza.domain.aggregates.usuarios

import co.com.alianza.app.MainActors

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterUsuarioAE }

/**
 * Created by S4N on 16/12/14.
 */
object ValidacionesAgenteEmpresarial {

  implicit val _: ExecutionContext = MainActors.dataAccesEx

  /*
  Este Metodo de validacionAgenteEmpresarial Me retorna el id de este usuario si cumple con los 3 parametros que se le envian a la DB
   */
  def validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int): Future[Validation[ErrorValidacion, Int]] = {
    val usuarioAgenteEmpresarialFuture = DataAccessAdapterUsuarioAE.validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int)
    usuarioAgenteEmpresarialFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (idUsuarioAgenteEmpresarial: Option[Int]) => idUsuarioAgenteEmpresarial match{
        case Some(x) =>
          println("%%%%%%%%%%%%%%%%%%%%%%%%%->>>>>> BIEN")
          println(x)
          println("%%%%%%%%%%%%%%%%%%%%%%%%%")
          zSuccess(x)
        case None =>
          println("%%%%%%%%%%%%%%%%%%%%%%%%%->>>>>> MAL")
          zFailure(ErrorAgenteEmpresarialNoExiste("Error... Agente Empresarial NO Existe"))
      }
    })
  }

}
