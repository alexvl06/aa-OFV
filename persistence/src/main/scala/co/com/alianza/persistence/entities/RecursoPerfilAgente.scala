package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
case class RecursoPerfilAgente(idPerfil: Int, urlRecurso: String, acceso: Boolean, filtro: Option[String])

class RecursoPerfilAgenteTable(tag: Tag) extends Table[RecursoPerfilAgente](tag, "RECURSO_PERFILAGENTE") {
  def idPerfil = column[Int]("ID_PERFIL", O.PrimaryKey)
  def urlRecurso = column[String]("URL_RECURSO", O.PrimaryKey)
  def acceso = column[Boolean]("ACCESO")
  def filtro = column[Option[String]]("FILTRO")

  def * = (idPerfil, urlRecurso, acceso, filtro) <> (RecursoPerfilAgente.tupled, RecursoPerfilAgente.unapply)
}
