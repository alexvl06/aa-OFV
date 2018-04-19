package enumerations.empresa

import enumerations.TipoIngresoUsuario.nextId

object TipoValidacion extends Enumeration {
  case class Val(idT: Int, name: String, isParam: Boolean) extends super.Val(nextId, name)
  val IP_CONFIANZA = Val(1, "Validar IP de confianza", false)
  val REINTENTOS = Val(2, "Validar Reintentos", false)
  val ESTADO_SIFI = Val(3, "Validar Estado SIFI", false)
  val CADUCIDAD_PASS = Val(4, "Validar caducidad contraseña", false)
}
