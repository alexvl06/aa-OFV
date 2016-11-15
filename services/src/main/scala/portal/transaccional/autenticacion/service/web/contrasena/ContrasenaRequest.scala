package portal.transaccional.autenticacion.service.web.contrasena

case class ReiniciarContrasenaAgente(usuario: String)

case class CambiarEstadoAgente(usuario: String)

case class CambiarContrasenaClienteAdminMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int])

case class CambiarContrasenaAgenteEmpresarialMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int])

case class CambiarContrasenaCaducadaClienteAdminMessage(token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int])

case class CambiarContrasenaCaducadaAgenteEmpresarialMessage(token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int])

/////////////////////////

case class CambiarContrasenaMessage(pw_actual: String, pw_nuevo: String, idUsuario: Option[Int])

case class CambiarContrasenaCaducadaRequestMessage(token: String, pw_actual: String, pw_nuevo: String)

case class CambiarContrasenaCaducadaMessage(token: String, pw_actual: String, pw_nuevo: String, us_id: Int, us_tipo: String)
