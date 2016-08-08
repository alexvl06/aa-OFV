package portal.transaccional.autenticacion.service.web.autenticacion

case class AutenticarRequest(tipoIdentificacion: Int, numeroIdentificacion: String, password: String)

case class AutenticarUsuarioEmpresarialRequest(nit: String, usuario: String, password: String)

case class AutenticarUsuarioComercialRequest(usuario: String, tipoUsuario: Int, contrasena: String)
