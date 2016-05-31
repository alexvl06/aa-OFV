package co.com.alianza.infrastructure.anticorruption.usuarios

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.persistence.entities.{
  Usuario => dUsuario,
  UsuarioEmpresarial => eUsuarioEmpresarial,
  UsuarioEmpresarialAdmin => eUsuarioEmpresarialAdmin,
  Empresa => dEmpresa,
  HorarioEmpresa => dHorarioEmpresa
}
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

  def translateHorarioEmpresa(horario: dHorarioEmpresa) = {
    HorarioEmpresa(horario.diaHabil, horario.sabado, horario.horaInicio, horario.horaFin)
  }

  def translateUsuario(usu: dUsuario) = {
    Usuario(usu.id, usu.correo, new Date(usu.fechaActualizacion.getTime), usu.identificacion, usu.tipoIdentificacion, usu.estado, usu.contrasena, usu.numeroIngresosErroneos, usu.ipUltimoIngreso, usu.fechaUltimoIngreso, TiposCliente.clienteIndividual)
  }

  def translateUsuarioEmpresarial(ue: eUsuarioEmpresarial) =
    UsuarioEmpresarial(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue.nombreUsuario))

  def translateTuplaUsuarioEmpresarialEstadoEmpresa(ue: (eUsuarioEmpresarial, Int)): (UsuarioEmpresarial, Int) = {
    (UsuarioEmpresarial(ue._1.id, ue._1.correo, ue._1.fechaActualizacion, ue._1.identificacion, ue._1.tipoIdentificacion, ue._1.usuario, ue._1.estado, ue._1.contrasena, ue._1.numeroIngresosErroneos, ue._1.ipUltimoIngreso, ue._1.fechaUltimoIngreso, TiposCliente.agenteEmpresarial, Some(ue._1.nombreUsuario)), ue._2)
  }

  def translateUsuarioEmpresarialAdmin(ue: eUsuarioEmpresarialAdmin) =
    UsuarioEmpresarialAdmin(ue.id, ue.correo, ue.fechaActualizacion, ue.identificacion, ue.tipoIdentificacion, ue.usuario, ue.estado, ue.contrasena, ue.numeroIngresosErroneos, ue.ipUltimoIngreso, ue.fechaUltimoIngreso, TiposCliente.clienteAdministrador)

  def translateUsuarioEmpresarialAdminEstadoEmpresa(ue: (eUsuarioEmpresarialAdmin, Int)): (UsuarioEmpresarialAdmin, Int) = {
    (UsuarioEmpresarialAdmin(ue._1.id, ue._1.correo, ue._1.fechaActualizacion, ue._1.identificacion, ue._1.tipoIdentificacion, ue._1.usuario, ue._1.estado, ue._1.contrasena, ue._1.numeroIngresosErroneos, ue._1.ipUltimoIngreso, ue._1.fechaUltimoIngreso, TiposCliente.clienteAdministrador), ue._2)
  }

  def translateUsuarioEmpresarialEmpresa(ue: eUsuarioEmpresarial) =
    UsuarioEmpresa(ue.id, ue.identificacion, ue.tipoIdentificacion)

  def translateUsuarioAdminEmpresa(ue: eUsuarioEmpresarialAdmin) =
    UsuarioEmpresa(ue.id, ue.identificacion, ue.tipoIdentificacion)

}