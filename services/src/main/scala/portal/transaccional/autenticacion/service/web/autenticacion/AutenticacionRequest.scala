package portal.transaccional.autenticacion.service.web.autenticacion

/**
 * Created by jonathan on 25/07/16.
 */
case class AutenticacionRequest(tipoIdentificacion: Int, numeroIdentificacion: String, password: String, clientIp: String)
