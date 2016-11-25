package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioInmobiliarioAuth
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario => eUsuarioAgenteInmob }
import enumerations.TipoAgenteInmobiliario
/**
 * Created by alexandra in 2016.
 */
object DataAccessTranslator {

  def entityToDto(agente: eUsuarioAgenteInmob): UsuarioInmobiliarioAuth = {
    val tipoAgente = if (agente.tipoAgente == TipoAgenteInmobiliario.internoAdmin.toString) TiposCliente.agenteInmobiliarioInterno else TiposCliente.agenteInmobiliario
    UsuarioInmobiliarioAuth(agente.id, tipoAgente, agente.identificacion, agente.tipoIdentificacion, agente.usuario)
  }

  def dtoToEntity(admin: UsuarioInmobiliarioAuth): eUsuarioAgenteInmob = ???
}