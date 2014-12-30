package co.com.alianza.infrastructure.messages.empresa

import java.sql.Timestamp

import co.com.alianza.infrastructure.messages.MessageService
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import enumerations.EstadosUsuarioEnum
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

/**
 * Created by S4N on 17/12/14.
 */

case class CrearAgenteEMessage(
                                 nit: String,
                                 usuario: String,
                                 nombre: String,
                                 apellido: String,
                                 cargo: String,
                                 correo: String,
                                 descripcion: String,
                                 ips: Array[String]
                              ) extends MessageService {

  def toEntityUsuarioAgenteEmpresarial():UsuarioEmpresarial = {
    UsuarioEmpresarial(0, correo, new Timestamp(System.currentTimeMillis()), nit, tipoIdentificacion = 3, usuario, EstadosUsuarioEnum.activo.id, contrasena = None, token = None, numeroIngresosErroneos = 0, ipUltimoIngreso = None, fechaUltimoIngreso = None, nombre, apellido, cargo, descripcion)
  }

}

object CrearAgenteEMessageJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val CrearAgenteEMessageMessageFormat = jsonFormat8(CrearAgenteEMessage)
}
