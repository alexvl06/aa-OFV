package co.com.alianza.infrastructure.dto

/**
 *
 * @author seven4n
 */
case class RecursoUsuario(idUsuario: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])
case class RecursoPerfilAgente(idUsuario: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])
case class RecursoPerfilClienteAdmin(idUsuario: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])
