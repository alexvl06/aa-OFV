package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RecursoBackPerfil(idRecurso: Int, idPerfil: Int)

class RecursoBackPerfilTable(tag: Tag) extends Table[RecursoBackPerfil](tag, "RECURSO_BACK_HAS_PERFIL") {

  def idRecurso = column[Int]("RECURSO_BACK_FK")
  def idPerfil = column[Int]("PERFIL_FK")

  def * = (idRecurso, idPerfil) <> (RecursoBackPerfil.tupled, RecursoBackPerfil.unapply)

}
