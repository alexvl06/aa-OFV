package co.com.alianza.infrastructure.messages.empresa

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.messages.MessageService
import co.com.alianza.persistence.entities.HorarioEmpresa
import spray.httpx.SprayJsonSupport
import java.sql.Time
import spray.json._

/**
 * @author hernando on 2/03/15.
 */
object HorarioEmpresaJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val AgregarHorarioEmpresaMessageFormat = jsonFormat6(AgregarHorarioEmpresaMessage)

}

case class ObtenerHorarioEmpresaMessage(idUsuario: Int, tipoCliente: TiposCliente) extends MessageService

case class AgregarHorarioEmpresaMessage(diaHabil: Boolean, sabado: Boolean, horaInicio: String, horaFin: String,
                                 idUsuario: Option[Int], tipoCliente: Option[Int]) extends MessageService