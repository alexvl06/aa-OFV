package portal.transaccional.autenticacion.service.drivers.usuarioAgente

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.persistence.entities.{ UsuarioAgente => eUsuarioEmpresarial }

/**
 * Created by alexandra on 29/07/16.
 */
object DataAccessTranslator {

  def entityToDto(agente: eUsuarioEmpresarial): UsuarioEmpresarial = {
    UsuarioEmpresarial(agente.id, agente.correo, agente.fechaActualizacion, agente.identificacion, agente.tipoIdentificacion, agente.usuario, agente.estado,
      agente.contrasena, agente.numeroIngresosErroneos, agente.ipUltimoIngreso, agente.fechaUltimoIngreso, TiposCliente.agenteEmpresarial,
      Option(agente.nombreUsuario))
  }

  def dtoToEntity(admin: UsuarioEmpresarial): eUsuarioEmpresarial = ???

}
