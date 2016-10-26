package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioInmobiliario
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario => eUsuarioAgenteInmob }
/**
 * Created by alexandra in 2016.
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioAgenteInmob) : UsuarioInmobiliario = {
    UsuarioInmobiliario(admin.identificacion, admin.tipoIdentificacion, admin.usuario, TiposCliente.agenteInmobiliario.id)
  }

  def dtoToEntity(admin: UsuarioInmobiliario): eUsuarioAgenteInmob = ???
}