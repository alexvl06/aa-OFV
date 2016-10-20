package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioAgenteInmobiliario
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario => eUsuarioAgenteInmob }
/**
 * Created by alexandra in 2016.
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioAgenteInmob) : UsuarioAgenteInmobiliario = {
    UsuarioAgenteInmobiliario(admin.id, admin.correo, admin.fechaActualizacion, admin.identificacion, admin.tipoIdentificacion, admin.usuario, admin.estado,
      admin.numeroIngresosErroneos, admin.ipUltimoIngreso, admin.fechaUltimoIngreso, TiposCliente.agenteInmobiliario)
  }

  def dtoToEntity(admin: UsuarioAgenteInmobiliario): eUsuarioAgenteInmob = ???

}