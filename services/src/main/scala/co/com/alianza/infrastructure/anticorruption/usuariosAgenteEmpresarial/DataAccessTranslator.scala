package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarial, UsuarioEmpresarialEstado, estadoUsuario, PinEmpresa }
import co.com.alianza.persistence.entities.{ PinEmpresa => ePinEmpresa, UsuarioEmpresarial => dUsuario }
import co.com.alianza.commons.enumerations.TiposCliente
import enumerations.EstadosEmpresaEnum

/**
 * Created by S4N on 22/12/14.
 */
object DataAccessTranslator {

  def translateEntityPinEmpresa(pin: PinEmpresa) = {
    ePinEmpresa(pin.id, pin.idUsuarioEmpresarial, pin.token, new Timestamp(pin.fechaExpiracion.getTime), pin.tokenHash, pin.uso)
  }

  def translateUsuarioEmpresarial(ue: dUsuario) =
    UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue.nombreUsuario))

  def translateUsuarioEstado(usuario: List[dUsuario]) = {
    usuario map (ue => UsuarioEmpresarialEstado(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, estadoUsuario(ue.estado, EstadosEmpresaEnum(ue.estado).toString), Some(""), ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue.nombreUsuario)))
  }

}