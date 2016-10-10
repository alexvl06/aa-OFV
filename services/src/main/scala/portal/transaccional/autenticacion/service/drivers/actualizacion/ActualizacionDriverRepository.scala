package portal.transaccional.autenticacion.service.drivers.actualizacion

import portal.transaccional.autenticacion.service.web.actualizacion._
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ActualizacionDAO

import scala.concurrent.{ Future, ExecutionContext }

/**
 * Created by hernando on 10/10/16.
 */
case class ActualizacionDriverRepository(actualizacionDAO: ActualizacionDAO)(implicit val ex: ExecutionContext) extends ActualizacionRepository {

  def obtenerPaises(): Future[Seq[Pais]] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerTiposCorreo(): Future[Seq[TipoCorreo]] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerOcupaciones(): Future[Seq[Ocupacion]] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerDatos(): Future[DatosCliente] = {
    Future.failed(new NotImplementedError())
  }

  def comprobarDatos(): Future[Boolean] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerCiudades(): Future[Seq[Ciudad]] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerEnviosCorrespondencia(): Future[Seq[EnvioCorrespondencia]] = {
    Future.failed(new NotImplementedError())
  }

  def obtenerActividadesEconomicas(): Future[Seq[ActividadEconomica]] = {
    Future.failed(new NotImplementedError())
  }

  def actualizarDatos(): Future[Boolean] = {
    Future.failed(new NotImplementedError())
  }

}
