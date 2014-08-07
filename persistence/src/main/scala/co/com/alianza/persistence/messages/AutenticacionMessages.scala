package co.com.alianza.persistence.messages

/**
 *
 * @author seven4n
 */
case class AutenticacionRequest( tipoIdentificacion:Int, numeroIdentificacion: String, password: String, clientIp:Option[String] )


case class AgregarIpHabitualRequest( tipoIdentificacion:Int, numeroIdentificacion: String, clientIp:Option[String] )


case class ValidarTokenRequest( token:String )
