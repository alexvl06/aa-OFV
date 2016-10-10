package portal.transaccional.autenticacion.service.drivers.actualizacion

import portal.transaccional.autenticacion.service.web.actualizacion._

import scala.concurrent.Future

/**
 * Created by hernando on 10/10/16.
 */
trait ActualizacionRepository {

  def obtenerPaises(): Future[Seq[Pais]]

  def obtenerTiposCorreo(): Future[Seq[TipoCorreo]]

  def obtenerOcupaciones(): Future[Seq[Ocupacion]]

  def obtenerDatos(): Future[DatosCliente]

  def comprobarDatos(): Future[Boolean]

  def obtenerCiudades(): Future[Seq[Ciudad]]

  def obtenerEnviosCorrespondencia(): Future[Seq[EnvioCorrespondencia]]

  def obtenerActividadesEconomicas(): Future[Seq[ActividadEconomica]]

  def actualizarDatos(): Future[Boolean]

}
