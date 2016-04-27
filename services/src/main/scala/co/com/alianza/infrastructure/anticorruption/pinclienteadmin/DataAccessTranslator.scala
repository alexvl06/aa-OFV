package co.com.alianza.infrastructure.anticorruption.pinclienteadmin

import java.util.Date

import co.com.alianza.infrastructure.dto.PinUsuarioEmpresarialAdmin
import co.com.alianza.persistence.entities.{ PinUsuarioEmpresarialAdmin => ePinUsuario }
import java.sql.Timestamp

/**
 * Created by manuel on 6/01/15.
 */
object DataAccessTranslator {

  def pinFromEntityToDto(ePin: ePinUsuario): PinUsuarioEmpresarialAdmin =
    PinUsuarioEmpresarialAdmin(ePin.id, ePin.idUsuario, ePin.token, new Date(ePin.fechaExpiracion.getTime), ePin.tokenHash)

  def translateEntityPinUsuario(pin: PinUsuarioEmpresarialAdmin) =
    ePinUsuario(pin.id, pin.idUsuario, pin.token, new Timestamp(pin.fechaExpiracion.getTime), pin.tokenHash)

}
