package co.com.alianza.infrastructure.anticorruption.usuarios

import co.com.alianza.persistence.entities.{Usuario => dUsuario, UsuarioEmpresarial => eUsuarioEmpresarial, UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin}
import co.com.alianza.infrastructure.dto._
import java.util.Date

/**
 *
 * @author seven4n
 */
object  DataAccessTranslator {

  def translateUsuario(usuario:List[dUsuario]) = {
   usuario map ( usu => Usuario(usu.id, usu.correo,new Date(usu.fechaActualizacion.getTime) , usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso, usu.fechaUltimaPeticion  ))
  }

  def translateUsuario(usu:dUsuario) = {
    Usuario(usu.id, usu.correo,new Date(usu.fechaActualizacion.getTime) , usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso, usu.fechaUltimaPeticion  )
  }

  def translateUsuarioEmpresarial (ue: eUsuarioEmpresarial) =
    UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso)

  def translateUsuarioEmpresarialAdmin (ue: eUsuarioEmpresarialAdmin) =
    UsuarioEmpresarialAdmin(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso)
}
