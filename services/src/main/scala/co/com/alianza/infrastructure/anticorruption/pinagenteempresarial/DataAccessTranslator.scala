package co.com.alianza.infrastructure.anticorruption.pinagenteempresarial

import java.util.Date

import co.com.alianza.infrastructure.dto.{PinUsuarioAgenteEmpresarial, PinUsuarioEmpresarialAdmin}
import co.com.alianza.persistence.entities.PinEmpresa
import java.sql.Timestamp

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessTranslator {

  def pinFromEntityToDto(pinEmpresa: PinEmpresa): PinUsuarioAgenteEmpresarial =
    PinUsuarioAgenteEmpresarial(pinEmpresa.id.getOrElse(0), pinEmpresa.idUsuarioEmpresarial, pinEmpresa.token, new Date(pinEmpresa.fechaExpiracion.getTime), pinEmpresa.tokenHash)

  def translateEntityPinEmpresa(pin:PinUsuarioAgenteEmpresarial, uso : Int) =
    PinEmpresa(Some(pin.id), pin.idUsuario, pin.token, new Timestamp(pin.fechaExpiracion.getTime), pin.tokenHash, uso)

}
