package co.com.alianza.infrastructure.messages.empresa

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.MessageService
import spray.httpx.SprayJsonSupport
import spray.json._

/**
 * @author hernando on 2/03/15.
 */
object HorarioEmpresaJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val DiaFestivoMessageFormat = jsonFormat1(DiaFestivoMessage)
  implicit val AgregarHorarioEmpresaMessageFormat = jsonFormat6(AgregarHorarioEmpresaMessage)

}

case class DiaFestivoMessage(fecha: String) extends MessageService

case class ObtenerHorarioEmpresaMessage(idUsuario: Int, tipoCliente: TiposCliente) extends MessageService

case class AgregarHorarioEmpresaMessage(diaHabil: Boolean, sabado: Boolean, horaInicio: String, horaFin: String,
  idUsuario: Option[Int], tipoCliente: Option[Int]) extends MessageService

case class ValidarHorarioEmpresaMessage(user: UsuarioAuth, idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]) extends MessageService
