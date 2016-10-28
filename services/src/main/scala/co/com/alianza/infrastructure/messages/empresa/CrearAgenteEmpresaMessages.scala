package co.com.alianza.infrastructure.messages.empresa

import java.sql.Timestamp

import co.com.alianza.infrastructure.messages.MessageService
import co.com.alianza.persistence.entities.UsuarioAgente
import enumerations.{ TipoIdentificacion, EstadosEmpresaEnum }
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by S4N on 17/12/14.
 */

object CrearAgenteEMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val crearAgenteEMessageMessageFormat = jsonFormat7(CrearAgenteMessage)
  implicit val actualizarAgenteEMessageMessageFormat = jsonFormat7(ActualizarAgenteMessage)
}

case class CrearAgenteMessage(
    tipoIdentificacion: Int,
    nit: String,
    usuario: String,
    nombre: String,
    cargo: String,
    correo: String,
    descripcion: String
) extends MessageService {

  def toEntityUsuarioAgenteEmpresarial(): UsuarioAgente = {
    UsuarioAgente(0, correo, new Timestamp(System.currentTimeMillis()), nit, tipoIdentificacion, usuario,
      EstadosEmpresaEnum.pendienteActivacion.id, contrasena = None, token = None, numeroIngresosErroneos = 0,
      ipUltimoIngreso = None, fechaUltimoIngreso = None, nombre, cargo, descripcion)
  }

}

case class ActualizarAgenteMessage(
  id: Int,
  nit: Option[String],
  usuario: String,
  nombreUsuario: String,
  cargo: String,
  correo: String,
  descripcion: String
) extends MessageService
