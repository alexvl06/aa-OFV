package portal.transaccional.autenticacion.service.web.comercial

/**
 * Created by s4n on 2016
 */
case class CrearAdministradorRequest(contrasena: String, usuario: String, nombre: String, correo: String)

case class ActualizarContrasenaRequest(contrasenaActual: String, contrasenaNueva: String)