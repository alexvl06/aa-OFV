package co.com.alianza.infrastructure.messages

import co.com.alianza.infrastructure.dto.Pais
import co.com.alianza.infrastructure.messages.AutenticacionMessagesJsonSupport._
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by hernando on 14/07/15.
 */

object ActualizacionMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PaisFormat = jsonFormat2(Pais)
  //implicit val AutenticarRequestMessageFormat = jsonFormat4(AutenticarMessage)
  //implicit val AutenticarClienteEmpresaMessageFormat = jsonFormat6(AutenticarUsuarioEmpresarialMessage)
  //implicit val AutorizarUrlRequestMessageFormat = jsonFormat2(AutorizarUrl)
  //implicit val AgregarIpHabitualRequestMessageFormat = jsonFormat2(AgregarIPHabitualUsuario)
}

case class ObtenerPaises extends MessageService
case class ObtenerCiudades extends MessageService
case class ObtenerTiposCorreo extends MessageService
case class ObtenerOcupaciones extends MessageService
case class ObtenerEnvioCorrespondencia extends MessageService
case class ObtenerActividadesEconomicas extends MessageService

class ActualizacionMessage {

}
