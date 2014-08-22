package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */
case class RecursoUsuario(idUsuario: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])

class RecursoUsuarioTable(tag: Tag) extends Table[RecursoUsuario](tag, "RECURSO_USUARIO") {
  def idUsuario   = column[Int]("ID_USUARIO", O.PrimaryKey)
  def urlRecurso  = column[String]("URL_RECURSO", O.PrimaryKey)
  def acceso      = column[Boolean]("ACCESO")
  def filtro      = column[Option[String]]("FILTRO")

  def * =  (idUsuario, urlRecurso, acceso, filtro) <> (RecursoUsuario.tupled, RecursoUsuario.unapply)
}