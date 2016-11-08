package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioInmobiliarioAuth
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario => eUsuarioAgenteInmob }
/**
 * Created by alexandra in 2016.
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioAgenteInmob): UsuarioInmobiliarioAuth = {
    UsuarioInmobiliarioAuth(admin.id, TiposCliente.agenteInmobiliario, admin.identificacion, admin.tipoIdentificacion, admin.usuario)
  }

  def dtoToEntity(admin: UsuarioInmobiliarioAuth): eUsuarioAgenteInmob = ???
}