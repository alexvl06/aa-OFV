package portal.transaccional.autenticacion.service.drivers.actualizacion

import co.com.alianza.util.json.MarshallableImplicits._
import portal.transaccional.autenticacion.service.web.actualizacion._

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translateDatosCliente(clienteJson: String): DatosCliente = {
    val datosCliente: Seq[DatosCliente] = clienteJson.fromJson[Seq[DatosCliente]]
    val datosEmpresa: Seq[DatosEmpresa] = clienteJson.fromJson[Seq[DatosEmpresa]]
    datosCliente.head.copy(datosEmp = datosEmpresa.head)
  }

  //Traducir paises
  def translatePaises(dataJson: String): Seq[Pais] = {
    dataJson.fromJson[Seq[Pais]]
  }

  //Traducir ciudades
  def translateCiudades(dataJson: String): Seq[Ciudad] = {
    dataJson.fromJson[Seq[Ciudad]]
  }

  //Traducir tipo correo
  def translateTiposCorreo(dataJson: String): Seq[TipoCorreo] = {
    dataJson.fromJson[Seq[TipoCorreo]]
  }

  //Traducir Envio Correspondencia
  def translateEnviosCorrespondencia(dataJson: String): Seq[EnvioCorrespondencia] = {
    dataJson.fromJson[Seq[EnvioCorrespondencia]]
  }

  //Traducir Ocupacion
  def translateOcupaciones(dataJson: String): Seq[Ocupacion] = {
    dataJson.fromJson[Seq[Ocupacion]]
  }

  //Traducir Actividad Economica
  def translateActividadesEconomicas(dataJson: String): Seq[ActividadEconomica] = {
    dataJson.fromJson[Seq[ActividadEconomica]]
  }

}
