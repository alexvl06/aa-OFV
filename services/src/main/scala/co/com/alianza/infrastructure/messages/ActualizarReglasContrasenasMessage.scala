package co.com.alianza.infrastructure.messages

import co.com.alianza.persistence.entities.ReglasContrasenas
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by david on 18/06/14.
 */
object ActualizarReglasContrasenasMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val ActualizarReglasContrasenasMessageFormat = jsonFormat1(ActualizarReglasContrasenasMessage)
}

case class ActualizarReglasContrasenasMessage(reglasContrasenas: List[Map[String, String]]) extends MessageService {
  def toEntityReglasContrasenas: List[ReglasContrasenas] = {
    for { regla <- reglasContrasenas; (key, value) <- regla } yield new ReglasContrasenas(key, value)
  }
}
