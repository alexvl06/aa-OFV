package co.com.alianza.infrastructure.anticorruption.actualizacion

import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.infrastructure.dto._

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translateDatosCliente(clienteJson: String): Option[DatosCliente] = {
    val datosCliente = clienteJson.fromJson[Array[DatosCliente]]
    val datosEmpresa = clienteJson.fromJson[Array[DatosEmpresa]]
    if (datosCliente.nonEmpty && datosEmpresa.nonEmpty)
      Some(datosCliente(0).copy(datosEmp = datosEmpresa(0)))
    else None
  }

  //Traducir paises
  def translatePaises(dataJson: String): Option[List[Pais]] = {
    val result = dataJson.fromJson[Array[Pais]]
    if (result.nonEmpty) Some(result.toList) else None
  }

  //Traducir ciudades
  def translateCiudades(dataJson: String): Option[List[Ciudad]] = {
    val result = dataJson.fromJson[Array[Ciudad]]
    if (result.nonEmpty) Some(result.toList) else None
  }

  //Traducir tipo correo
  def translateTiposCorreo(dataJson: String): Option[List[TipoCorreo]] = {
    val result = dataJson.fromJson[Array[TipoCorreo]]
    if (result.nonEmpty) Some(result.toList) else None
  }

  //Traducir Envio Correspondencia
  def translateEnviosCorrespondencia(dataJson: String): Option[List[EnvioCorrespondencia]] = {
    val result = dataJson.fromJson[Array[EnvioCorrespondencia]]
    if (result.nonEmpty) Some(result.toList) else None
  }

  //Traducir Ocupacion
  def translateOcupaciones(dataJson: String): Option[List[Ocupacion]] = {
    val result = dataJson.fromJson[Array[Ocupacion]]
    if (result.nonEmpty) Some(result.toList) else None
  }

  //Traducir Actividad Economica
  def translateActividadesEconomicas(dataJson: String): Option[List[ActividadEconomica]] = {
    val result = dataJson.fromJson[Array[ActividadEconomica]]
    if (result.nonEmpty) Some(result.toList) else None
  }

}