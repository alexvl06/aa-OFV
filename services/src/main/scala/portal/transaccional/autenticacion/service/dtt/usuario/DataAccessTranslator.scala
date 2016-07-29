package portal.transaccional.autenticacion.service.dtt.usuario

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.persistence.entities.{ Usuario => eUsuario }

/**
 * Created by alexandra on 27/07/16.
 */
object DataAccessTranslator {

  def entityToDto(usuario: eUsuario): Usuario = {
    Usuario(usuario.id, usuario.correo, usuario.fechaActualizacion, usuario.identificacion, usuario.tipoIdentificacion, usuario.estado,
      usuario.numeroIngresosErroneos, usuario.ipUltimoIngreso, usuario.fechaUltimoIngreso, TiposCliente.clienteIndividual)
  }

  def dtoToEntity(usuario: Usuario): eUsuario = ???

}
