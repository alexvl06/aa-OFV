package co.com.alianza.infrastructure.dto

/**
 *
 * @author seven4n
 */
case class RecursoUsuario(idUsuario: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])
