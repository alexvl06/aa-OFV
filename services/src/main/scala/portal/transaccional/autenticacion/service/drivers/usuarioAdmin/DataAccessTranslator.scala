package portal.transaccional.autenticacion.service.drivers.usuarioAdmin

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.persistence.entities.{ UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin }

/**
 * Created by alexandra in 2016.
 */
object DataAccessTranslator {

  def entityToDto(admin: eUsuarioEmpresarialAdmin, tipoCliente: String): UsuarioEmpresarialAdmin = {
    val tipoAdmin = if (tipoCliente == TiposCliente.clienteAdministrador.toString) TiposCliente.clienteAdministrador else TiposCliente.clienteAdminInmobiliario
    UsuarioEmpresarialAdmin(admin.id, admin.correo, admin.fechaActualizacion, admin.identificacion, admin.tipoIdentificacion, admin.usuario, admin.estado,
      admin.contrasena, admin.numeroIngresosErroneos, admin.ipUltimoIngreso, admin.fechaUltimoIngreso, tipoAdmin)
  }

  def dtoToEntity(admin: UsuarioEmpresarialAdmin): eUsuarioEmpresarialAdmin = ???

}
