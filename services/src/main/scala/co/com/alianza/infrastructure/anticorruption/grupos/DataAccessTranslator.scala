package co.com.alianza.infrastructure.anticorruption.grupos

import co.com.alianza.infrastructure.dto.{ MiembroGrupo, Cliente }
import co.com.alianza.util.json.MarshallableImplicits._

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {
  def translateCliente(clienteJson: String): Option[Cliente] = {
    val result = clienteJson.fromJson[Array[MiembroGrupo]]
    if (result nonEmpty) Some(result(0).toCliente) else None
  }
}

