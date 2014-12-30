package co.com.alianza.infrastructure.anticorruption.empresa.usuarios


import co.com.alianza.persistence.entities.{UsuarioEmpresarial => dUsuario}
import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.commons.enumerations.TiposCliente

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  def translateUsuario(usuario:List[dUsuario]) = {
   usuario map ( ue => UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, ue.nombreUsuario))
  }

}
