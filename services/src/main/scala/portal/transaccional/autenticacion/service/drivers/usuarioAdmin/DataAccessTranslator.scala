package portal.transaccional.autenticacion.service.drivers.usuarioAdmin

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.persistence.entities.{ UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin }
import co.com.alianza.util.token.Token

/**
 * Created by alexandra on 27/07/16.
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioEmpresarialAdmin): UsuarioEmpresarialAdmin = {
    UsuarioEmpresarialAdmin(admin.id, admin.correo, admin.fechaActualizacion, admin.identificacion, admin.tipoIdentificacion, admin.usuario, admin.estado,
      admin.contrasena, admin.numeroIngresosErroneos, admin.ipUltimoIngreso, admin.fechaUltimoIngreso, TiposCliente.clienteAdministrador)
  }

  def dtoToEntity(admin: UsuarioEmpresarialAdmin): eUsuarioEmpresarialAdmin = ???

}
