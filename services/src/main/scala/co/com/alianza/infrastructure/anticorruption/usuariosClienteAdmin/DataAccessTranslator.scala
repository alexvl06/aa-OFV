package co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin

import co.com.alianza.persistence.entities.{ UsuarioEmpresarialAdmin => pUsuarioEmpresarialAdmin }
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import java.util.Date
import co.com.alianza.commons.enumerations.TiposCliente

/**
 * Created by josegarcia on 30/01/15.
 */
object DataAccessTranslator {

  def translateUsuario(ue: pUsuarioEmpresarialAdmin) = {
    UsuarioEmpresarialAdmin(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.clienteAdministrador)
  }

}
