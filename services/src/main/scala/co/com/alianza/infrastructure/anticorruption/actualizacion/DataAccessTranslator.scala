package co.com.alianza.infrastructure.anticorruption.actualizacion

import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.infrastructure.dto.{Pais, DatosCliente}

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  /*
  def translateDatosCliente(clienteJson: String): Option[DatosCliente] = {
    val result = clienteJson.fromJson[Array[DatosCliente]]
    if (result nonEmpty) Some(result(0)) else None
  }
  */

  def translatePaises(dataJson: String): Option[List[Pais]] = {
    val result = dataJson.fromJson[Array[Pais]]
    if (result nonEmpty) Some(result.toList) else None
  }
  
}