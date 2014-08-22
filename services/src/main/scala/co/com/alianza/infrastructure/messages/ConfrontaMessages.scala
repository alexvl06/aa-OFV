package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by ricardoseven on 16/06/14.
 */

object ConfrontaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ObtenerCuestionarioRequestMessageFormat = jsonFormat4(ObtenerCuestionarioRequestMessage)
  implicit val ObtenerCuestionarioAdicionalRequestMessageFormat = jsonFormat7(ObtenerCuestionarioAdicionalRequestMessage)
  implicit val ValidarCuestionarioRequestMessageFormat = jsonFormat3(ValidarCuestionarioRequestMessage)
}

case class ObtenerCuestionarioRequestMessage(primerApellido:String, codigoTipoIdentificacion:String, numeroIdentificacion: String, fechaExpedicion:String) extends MessageService {
}

case class ObtenerCuestionarioAdicionalRequestMessage(codigoCiudad:Int, codigoCuestionario:Int, codigoDepartamento:Int, codigoTipoIdentificacion:String, numeroIdentificac: String, telefono:String, secuenciaCuestionario:Long) extends MessageService {
}

case class ValidarCuestionarioRequestMessage(respuestas:Array[String],secuenciaCuestionario:Long,codigoCuestionario:Int) extends MessageService {
}
