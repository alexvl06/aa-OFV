package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.infrastructure.dto.PinEmpresa
import co.com.alianza.persistence.entities.{PinEmpresa => ePinEmpresa}

/**
 * Created by S4N on 22/12/14.
 */
object DataAccessTranslator {

  def translateEntityPinEmpresa(pin: PinEmpresa) = {
    ePinEmpresa(pin.id, pin.idUsuarioEmpresarial, pin.token,new Timestamp(pin.fechaExpiracion.getTime), pin.tokenHash, pin.uso)
  }

}