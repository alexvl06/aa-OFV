package co.com.alianza.infrastructure.messages

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import co.com.alianza.persistence.messages.ObtenerCuestionarioRequest
import co.com.alianza.persistence.messages.ValidarCuestionarioRequest
import co.com.alianza.persistence.messages.ObtenerCuestionarioAdicionalRequest

/**
 * Created by ricardoseven on 16/06/14.
 */

object ConfrontaMessagesJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ObtenerCuestionarioRequestMessageFormat = jsonFormat6(ObtenerCuestionarioRequestMessage)
  implicit val ObtenerCuestionarioAdicionalRequestMessageFormat = jsonFormat7(ObtenerCuestionarioAdicionalRequestMessage)
  implicit val ValidarCuestionarioRequestMessageFormat = jsonFormat3(ValidarCuestionarioRequestMessage)
}

case class ObtenerCuestionarioRequestMessage(codigoCiudad:Int, codigoCuestionario:Int, codigoDepartamento:Int, codigoTipoIdentificacion:String, numeroIdentificacion: String, telefono:String) extends MessageService {
  def toObtenerCuestionario:ObtenerCuestionarioRequest = ObtenerCuestionarioRequest(codigoCiudad,
                                                                                    codigoCuestionario,
                                                                                    codigoDepartamento,
                                                                                    codigoTipoIdentificacion,
                                                                                    numeroIdentificacion,
                                                                                    telefono)
}

case class ObtenerCuestionarioAdicionalRequestMessage(codigoCiudad:Int, codigoCuestionario:Int, codigoDepartamento:Int, codigoTipoIdentificacion:String, numeroIdentificac: String, telefono:String, secuenciaCuestionario:Long) extends MessageService {
  def toObtenerCuestionarioAdicional:ObtenerCuestionarioAdicionalRequest = ObtenerCuestionarioAdicionalRequest(codigoCiudad,
                                                                  codigoCuestionario,
                                                                  codigoDepartamento,
                                                                  codigoTipoIdentificacion,
                                                                  numeroIdentificac,
                                                                  telefono,
                                                                  secuenciaCuestionario)
}

case class ValidarCuestionarioRequestMessage(codigoCuestionario:Int,respuestas:String ,secuenciaCuestionario:Int) extends MessageService {
  def toValidarCuestionarioRequest: ValidarCuestionarioRequest = ValidarCuestionarioRequest(codigoCuestionario,
                                                                                            respuestas,
                                                                                            secuenciaCuestionario)
}
