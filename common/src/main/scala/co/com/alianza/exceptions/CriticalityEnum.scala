package co.com.alianza.infrastructure.exceptions

object CriticalityEnum extends Enumeration {

  type value = Value

  val DEBUG, INFO, WARN, ERROR, FATAL = Value
}