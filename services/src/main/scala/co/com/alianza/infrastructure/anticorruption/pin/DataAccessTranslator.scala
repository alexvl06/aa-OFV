package co.com.alianza.infrastructure.anticorruption.pin

import java.util.Date

import co.com.alianza.infrastructure.dto.PinUsuario
import co.com.alianza.persistence.entities.{PinUsuario => ePinUsuario}

object DataAccessTranslator {

  def pinFromEntityToDto(ePin: ePinUsuario): PinUsuario = {
    PinUsuario(ePin.id, ePin.idUsuario, ePin.token, new Date(ePin.fechaExpiracion.getTime), ePin.tokenHash)
  }

}