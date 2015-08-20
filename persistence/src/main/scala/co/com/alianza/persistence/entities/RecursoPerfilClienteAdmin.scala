package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
case class RecursoPerfilClienteAdmin(idPerfil: Int, urlRecurso: String, acceso:Boolean, filtro:Option[String])

class RecursoPerfilClienteAdminTable(tag: Tag) extends Table[RecursoPerfilClienteAdmin](tag, "RECURSO_PERFILCLIENTEADMIN") {
  def idPerfil   = column[Int]("ID_PERFIL", O.PrimaryKey)
  def urlRecurso  = column[String]("URL_RECURSO", O.PrimaryKey)
  def acceso      = column[Boolean]("ACCESO")
  def filtro      = column[Option[String]]("FILTRO")

  def * =  (idPerfil, urlRecurso, acceso, filtro) <> (RecursoPerfilClienteAdmin.tupled, RecursoPerfilClienteAdmin.unapply)
}
