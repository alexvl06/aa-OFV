package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioAgenteInmobiliario
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario => eUsuarioAgenteInmobiliario }

/**
 * Created by alexandra in 2016
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioAgenteInmobiliario): UsuarioAgenteInmobiliario = {
    UsuarioAgenteInmobiliario(admin.id, admin.correo, admin.identificacion, admin.tipoIdentificacion, admin.usuario, admin.estado,
      Option.empty, admin.numeroIngresosErroneos, admin.ipUltimoIngreso, TiposCliente.agenteInmobiliario)
  }

  def dtoToEntity(admin: UsuarioAgenteInmobiliario): eUsuarioAgenteInmobiliario = ???

}
