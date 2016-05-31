package co.com.alianza.infrastructure.anticorruption.clientes

import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.infrastructure.dto.{ Cliente, MiembroGrupo }

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {
  def translateCliente(clienteJson: String): Option[Cliente] = {
    val result = clienteJson.fromJson[Array[Cliente]]
    if (result nonEmpty) Some(result(0)) else None
  }
  def translateGrupo(clienteJson: String): Option[Cliente] = {
    val result = clienteJson.fromJson[Array[MiembroGrupo]]
    if (result.nonEmpty) Some(result(0).toCliente) else None
  }

}

