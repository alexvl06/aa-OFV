package co.com.alianza.domain.aggregates.autenticacion.errores

import co.com.alianza.infrastructure.messages.ErrorMessage
import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.{ UsuarioEmpresarial, UsuarioEmpresarialAdmin, RecursoPerfilAgente, RecursoPerfilClienteAdmin }

/**
 * Created by manuel on 3/03/15.
 */
sealed trait ErrorAutorizacion {
  def msg: String
}

case class ErrorPersistenciaAutorizacion(msg: String, e: PersistenceException) extends ErrorAutorizacion

case class RecursoInexistente(usuario: UsuarioEmpresarial) extends ErrorAutorizacion {
  override def msg = ErrorMessage("403.1", "Error autorización de recurso", "El recurso no está registrado").toJson
}

case class RecursoRestringido(usuario: UsuarioEmpresarial, filtro: Option[String] = None) extends ErrorAutorizacion {
  override def msg = ErrorMessage("403.2", "Error autorización de recurso", "El acceso a este recurso está restringido").toJson
}

case class ErrorSesionNoEncontrada() extends ErrorAutorizacion {
  override def msg = ErrorMessage("403.9", "Error sesión", "No se ha encontrado la sesión").toJson
}

case class ErrorSesionIpInvalida(ip: String) extends ErrorAutorizacion {
  override def msg = ErrorMessage("401.21", "Error sesión", s"La ip de acceso '$ip' no está permitida").toJson
}

case class ErrorSesionHorarioInvalido() extends ErrorAutorizacion {
  override def msg = ErrorMessage("401.22", "Error sesión", "No está permitido el acceso en este horario.").toJson
}

case class ErrorSesionEstadoEmpresaDenegado() extends ErrorAutorizacion {
  override def msg = ErrorMessage("401.23", "Error sesión", "Acceso denegado para esta empresa").toJson
}

case class TokenInvalido() extends ErrorAutorizacion {
  override def msg = ErrorMessage("401.24", "Error token", "El token no es válido").toJson
}

case class ErrorAlObtenerAgenteConEstado() extends ErrorAutorizacion {
  override def msg = ErrorMessage("401.25", "Error al obtener agente ", "El token no es válido").toJson
}