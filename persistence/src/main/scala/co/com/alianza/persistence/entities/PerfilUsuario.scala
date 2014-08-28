package co.com.alianza.persistence.entities

import CustomDriver.simple._
/**
 *
 * @author smontanez
 */
case class PerfilUsuario (idUsuario: Int, idPerfil: Int)

class PerfilUsuarioTable(tag: Tag) extends Table[PerfilUsuario](tag, "PERFIL_USUARIO") {

  def idUsuario      = column[Int]("ID_USUARIO", O.PrimaryKey)
  def idPerfil  = column[Int]("ID_PERFIL",O.PrimaryKey)

  def * =  (idUsuario, idPerfil) <> (PerfilUsuario.tupled, PerfilUsuario.unapply)
}