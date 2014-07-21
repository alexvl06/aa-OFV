package co.com.alianza.persistence.messages

/**
 *
 * @author seven4n
 */
case class AutenticacionRequest( tipoIdentificacion:String, numeroIdentificacion: String, password: String, clientIp:Option[String] )


case class AgregarIpHabitualRequest( tipoIdentificacion:String, numeroIdentificacion: String, clientIp:Option[String] )


case class ValidarTokenRequest( token:String )
