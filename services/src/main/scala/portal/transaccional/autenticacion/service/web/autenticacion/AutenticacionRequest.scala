package portal.transaccional.autenticacion.service.web.autenticacion

/**
 * Created by jonathan on 25/07/16.
 */
case class AutenticarRequest(tipoIdentificacion: Int, numeroIdentificacion: String, password: String)

case class AutenticarUsuarioEmpresarialRequest(tipoIdentificacion: Int, numeroIdentificacion: String, usuario: String, password: String)
