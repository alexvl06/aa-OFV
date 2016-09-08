package co.com.alianza.exceptions

import scala.util.control.NoStackTrace

/**
 * Representa un error general al ejecutar alguna petición
 *
 * @param cause Traza del error generado
 * @param level Nivel de la excepción
 * @param message Mensaje de la excepción
 * @param currentTime Fecha generación del error
 * @author seven4n
 */
sealed class AlianzaException(val cause: Throwable, val level: LevelException, val message: String, val currentTime: Long) extends RuntimeException(cause)

/**
 * Representa una excepción de persistencia
 *
 * @param cause Traza del error generado
 * @param level Nivel de la excepción
 * @param message Mensaje de la excepción
 * @param currentTime Fecha generación del error
 * @author seven4n
 */
case class PersistenceException(override val cause: Throwable, override val level: LevelException, override val message: String, override val currentTime: Long) extends AlianzaException(cause, level, message, currentTime)

/**
 *
 * Representa un error generado al consumir un servicio
 *
 * @param cause Traza del error generado
 * @param level Nivel de la excepción
 * @param message Mensaje de la excepción
 * @param currentTime Fecha generación del error
 * @author seven4n
 */
case class ServiceException(override val cause: Throwable, override val level: LevelException, override val message: String, override val currentTime: Long, statusCode: Option[Int] = None, bodyError: Option[String] = None) extends AlianzaException(cause, level, message, currentTime)

case class ValidacionException(code: String, data: String) extends NoStackTrace {

  override def getMessage: String = s"code: $code; data: $data"
}

object PersistenceException {
  def apply(exc: Throwable, level: LevelException, message: String) = new PersistenceException(exc, level, message, currentTime = System.currentTimeMillis())
}

object ServiceException {
  def apply(exc: Throwable, level: LevelException, message: String) = new ServiceException(exc, level, message, currentTime = System.currentTimeMillis())
  def apply(exc: Throwable, level: LevelException, message: String, statusCode: Int, bodyError: String) = new ServiceException(exc, level, message, currentTime = System.currentTimeMillis(), Some(statusCode), Some(bodyError))
}

object AlianzaException {
  def apply(exc: Throwable, level: LevelException, message: String) = new AlianzaException(exc, level, message, currentTime = System.currentTimeMillis())
}

/**
 * Validaciones autorizacion token
 */
abstract class ValidacionAutorizacion() extends NoStackTrace
case class NoAutorizado(codigo: String) extends ValidacionAutorizacion
case class Autorizado(usuario: String) extends ValidacionAutorizacion
case class AutorizadoComercial(usuario: String) extends ValidacionAutorizacion
case class AutorizadoComercialAdmin(usuario: String) extends ValidacionAutorizacion
case class Prohibido(codigo: String, usuario: String) extends ValidacionAutorizacion

class ExpiredPasswordException(msg: String) extends Exception(msg)
