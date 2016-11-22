package portal.transaccional.autenticacion.service.web.contrasena

case class ReiniciarContrasenaAgente(usuario: String)

case class CambiarEstadoAgente(usuario: String)

case class CambiarContrasena(pw_actual: String, pw_nuevo: String)

/////////////////////////

case class CambiarContrasenaCaducada(token: String, pw_actual: String, pw_nuevo: String)
