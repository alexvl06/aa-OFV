package portal.transaccional.autenticacion.service.web.ip

/**
 * Created by s4n on 2016
 */
case class AgregarIpRequest(idUsuario: Option[Int], clientIp: Option[String] = None)