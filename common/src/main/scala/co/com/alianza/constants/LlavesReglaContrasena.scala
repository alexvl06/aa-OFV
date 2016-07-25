package co.com.alianza.constants

case class LlaveReglaContrasena(llave: String)

object LlavesReglaContrasena extends Enumeration {

  protected case class Val(llave: String) extends super.Val(nextId, llave) {}

  val DIAS_VALIDA = Val("DIAS_VALIDA")

}