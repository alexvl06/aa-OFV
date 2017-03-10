package co.com.alianza.constants

object LlavesReglaContrasena extends Enumeration {

  case class Val(llave: String) extends super.Val(nextId, llave) {}

  type llavesReglaContrasena = Value

  val DIAS_VALIDA = Val("DIAS_VALIDA")
  val CANTIDAD_REINTENTOS_INGRESO_CONTRASENA = Val("CANTIDAD_REINTENTOS_INGRESO_CONTRASENA")

  val ULTIMAS_CONTRASENAS_NO_VALIDAS = Val("ULTIMAS_CONTRASENAS_NO_VALIDAS")

}