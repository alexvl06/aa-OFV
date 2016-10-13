package enumerations

/**
 * Enumeración que contiene los posibles errores producidos al validar un PIN
 */
object EstadosPin extends Enumeration(1) {

  type EstadoPin = Value

  val PIN_NO_EXISTE: EstadoPin = Value(1, "El pin no existe")
  val PIN_INVALIDO: EstadoPin = Value(2, "El pin es inválido")
  val PIN_CADUCADO: EstadoPin = Value(3, "El pin está caducado")
}
