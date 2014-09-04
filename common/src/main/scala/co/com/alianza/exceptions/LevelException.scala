package co.com.alianza.exceptions

sealed trait LevelException

/**
 * Representa un error de negocio. De manejo de datos
 *
 * @author seven4n
 */
case object BusinessLevel extends LevelException

/**
 * Representa una excepción técnica
 *
 * @author seven4n
 */
case object TechnicalLevel extends LevelException

/**
 * Representa un error Técnico Timeout
 *
 * @author seven4n
 */
case object TimeoutLevel extends LevelException


/**
 * Representa un error interno generado en un microservicio
 *
 * @author seven4n
 */
case object InternalServiceLevel extends LevelException

/**
 * Representa un error Técnico de Conexión
 *
 * @author seven4n
 */
case object NetworkLevel extends LevelException
