package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial, PinEmpresa}
import co.com.alianza.persistence.entities.{PinEmpresa => ePinEmpresa, UsuarioEmpresarial}
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.persistence.entities.{UsuarioEmpresarial => dUsuario}

/**
 * Created by S4N on 22/12/14.
 */
object DataAccessTranslator {

  def translateEntityPinEmpresa(pin: PinEmpresa) = {
    ePinEmpresa(pin.id, pin.idUsuarioEmpresarial, pin.token,new Timestamp(pin.fechaExpiracion.getTime), pin.tokenHash, pin.uso)
  }

  def translateUsuario(usuario:List[dUsuario]) = {
    usuario map ( ue => UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue.nombreUsuario)))
  }

}