package co.com.alianza.domain.aggregates.autenticacion.errores

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.messages.ErrorMessage
import co.com.alianza.util.json.MarshallableImplicits._

sealed trait ErrorAutenticacion {
  def msg: String
}

case class ErrorClienteInactivoCore() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.1", "Error Cliente Alianza", "Cliente inactivo en core de alianza").toJson
}

case class ErrorClienteNoExisteCore() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.2", "Error Cliente Alianza", "No existe el cliente en el core de alianza").toJson
}

case class ErrorCredencialesInvalidas() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.3", "Error Credenciales", "Credenciales invalidas para acceder al portal de alianza fiduciaria").toJson
}

case class ErrorPasswordInvalido(identificacionUsuario: Option[String], idUsuario: Option[Int], numIngresosErroneosUsuario: Int) extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.3", "Error Credenciales", "Credenciales invalidas para acceder al portal de alianza fiduciaria").toJson
}

case class ErrorControlIpsDesactivado(token: String) extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.4", "Control IP", "El usuario no tiene activo el control de direcciones ip", token).toJson
}

case class ErrorIntentosIngresoInvalidos() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.7", "Usuario Bloqueado", "Ha excedido el numero máximo intentos permitidos al sistema, su usuario ha sido bloqueado").toJson
}

case class ErrorUsuarioBloqueadoIntentosErroneos() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.8", "Usuario Bloqueado", "El usuario se encuentra bloqueado").toJson
}

case class ErrorPasswordCaducado(token: String) extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.9", "Error Credenciales", "La contraseña del usuario ha caducado", token).toJson
}

case class ErrorUsuarioBloqueadoPendienteActivacion() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.10", "Usuario Bloqueado", "El usuario se encuentra pendiente de activación").toJson
}

case class ErrorUsuarioBloqueadoPendienteReinicio() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.12", "Usuario Bloqueado", "El usuario se encuentra bloqueado pendiente de reiniciar contraseña").toJson
}

case class ErrorUsuarioBloqueadoCorreoVacio() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.13", "Usuario Bloqueado", "No hay correo registrado en la base de datos de Alianza").toJson
}

case class ErrorUsuarioDesactivadoSuperAdmin() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.14", "Usuario Desactivado", "El usuario se encuentra desactivado por superadministrador").toJson
}

case class ErrorHorarioIngresoEmpresa() extends ErrorAutenticacion {
  override def msg = ErrorMessage("401.15", "Horario Empresa", "Está intentando ingresar a la aplicación en un horario no hábil.").toJson
}

// Error de persistencia
case class ErrorPersistencia(msg: String, e: PersistenceException) extends ErrorAutenticacion

// Error regla
case class ErrorRegla(regla: String) extends ErrorAutenticacion {
  override def msg = "Error al obtener la regla " + regla
}