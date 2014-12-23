package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import co.com.alianza.persistence.entities.{PinEmpresa => ePinEmpresa}
import co.com.alianza.persistence.repositories.UsuariosEmpresarialRepository
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import enumerations.EstadosEmpresaEnum

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

/**
 * Created by S4N on 16/12/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  val repo = new UsuariosEmpresarialRepository()

  def validacionAgenteEmpresarial( numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, Option[Int]]] = {
    repo.validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int)
  }

  def CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado : EstadosEmpresaEnum.estadoEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado)
  }

  def crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial: ePinEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.guardarPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)
  }

}
