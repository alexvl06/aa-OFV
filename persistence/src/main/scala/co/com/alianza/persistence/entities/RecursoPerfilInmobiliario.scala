package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RecursoPerfilInmobiliario(idPerfil: Int, urlRecurso: Int)

class RecursoPerfilInmobiliarioTable(tag: Tag) extends Table[RecursoPerfilInmobiliario](tag, "RECURSO_PERFIL_INMOBILIARIO") {

  def idPerfil = column[Int]("ID_PERFIL", O.PrimaryKey)
  def urlRecurso = column[Int]("ID_RECURSO", O.PrimaryKey)

  def * = (idPerfil, urlRecurso) <> (RecursoPerfilInmobiliario.tupled, RecursoPerfilInmobiliario.unapply)
}
