package co.com.alianza.infrastructure.anticorruption.usuarios

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.persistence.entities.{Usuario => dUsuario, UsuarioEmpresarial => eUsuarioEmpresarial, UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin, Empresa => dEmpresa}
import co.com.alianza.infrastructure.dto._
import java.util.Date

/**
 *
 * @author seven4n
 */
object DataAccessTranslator {

  def translateUsuario(usuario: List[dUsuario]) = {
    usuario map (usu => Usuario(usu.id, usu.correo, new Date(usu.fechaActualizacion.getTime), usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso, TiposCliente.clienteIndividual))
  }

  def translateEmpresa(emp: dEmpresa) = {
    Empresa(emp.id, emp.nit, emp.estadoEmpresa)
  }

  def translateUsuario(usu: dUsuario) = {
    Usuario(usu.id, usu.correo, new Date(usu.fechaActualizacion.getTime), usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso, TiposCliente.clienteIndividual)
  }

  def translateUsuarioEmpresarial(ue: eUsuarioEmpresarial) =
    UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue.nombreUsuario))

  def translateUsuarioEmpresarialAdmin(ue: eUsuarioEmpresarialAdmin) =
    UsuarioEmpresarialAdmin(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.clienteAdministrador)
}
