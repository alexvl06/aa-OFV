package co.com.alianza.infrastructure.anticorruption.empresa.usuarios

import co.com.alianza.constants.{TipoIdentificacion, EstadosUsuarioEnum}
import co.com.alianza.persistence.entities.{Usuario => dUsuario, UsuarioEmpresarialAdmin => dUsuarioEmpresarialAdmin, UsuarioAdmin => dUsuarioAdmin, PinUsuario=> ePinUsuario, IpUsuario => dIpUsuario}
import co.com.alianza.infrastructure.dto._
import java.util.Date
import java.sql.Timestamp

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  def translateUsuario(usuario:List[dUsuario]) = {
   usuario map ( usu => Usuario(usu.id, usu.correo,new Date(usu.fechaActualizacion.getTime) , usu.identificacion, tipoIdentificacionDto(usu.tipoIdentificacion, TipoIdentificacion(usu.tipoIdentificacion).toString), estadoUsuario(usu.estado, EstadosUsuarioEnum(usu.estado).toString), Some(""), usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso ))
  }

}
