package enumerations

/**
 * Created by hernando on 8/06/16.
 */
object ConfiguracionEnum extends Enumeration(1) {

  case class Val(name: String) extends super.Val(nextId, name) {}

  type configuracion = Value

  val AUTOVALIDACION_NUMERO_PREGUNTAS = Val("AUTOVALIDACION_NUMERO_PREGUNTAS")
  val AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION = Val("AUTOVALIDACION_NUMERO_PREGUNTAS_COMPROBACION")
  val AUTOVALIDACION_NUMERO_PREGUNTAS_CAMBIAR = Val("AUTOVALIDACION_NUMERO_PREGUNTAS_CAMBIAR")
  val AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA = Val("AUTOVALIDACION_NUMERO_PREGUNTAS_LISTA")
  val AUTOVALIDACION_NUMERO_REINTENTOS = Val("AUTOVALIDACION_NUMERO_REINTENTOS")
  val EXPIRACION_PIN = Val("EXPIRACION_PIN")
  val EXPIRACION_SESION = Val("EXPIRACION_SESION")

}
