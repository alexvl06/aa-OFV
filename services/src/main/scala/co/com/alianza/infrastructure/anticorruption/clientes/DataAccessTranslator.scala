package co.com.alianza.infrastructure.anticorruption.clientes

import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.infrastructure.dto.Cliente

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {
 def translateCliente(clienteJson:String): Option[Cliente] = {
   println(clienteJson)
   val result = clienteJson.fromJson[Array[Cliente]]
   if(result nonEmpty) Some(result(0)) else None
 }
}

