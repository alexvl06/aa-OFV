package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 *
 * @author seven4n
 */
case class RecursoPerfil(idPerfil: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])

class RecursoPerfilTable(tag: Tag) extends Table[RecursoPerfil](tag, "RECURSO_PERFIL") {
  def idPerfil   = column[Int]("ID_PERFIL", O.PrimaryKey)
  def urlRecurso  = column[String]("URL_RECURSO", O.PrimaryKey)
  def acceso      = column[Boolean]("ACCESO")
  def filtro      = column[Option[String]]("FILTRO")

  def * =  (idPerfil, urlRecurso, acceso, filtro) <> (RecursoPerfil.tupled, RecursoPerfil.unapply)
}