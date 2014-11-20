package co.com.alianza.constants

case class TiposConfiguracion(llave: String)

object TiposConfiguracion extends Enumeration {

  protected case class Val( llave:String ) extends super.Val(nextId, llave) {}

  val EXPIRACION_PIN = Val( "Expiración Pin" )

}