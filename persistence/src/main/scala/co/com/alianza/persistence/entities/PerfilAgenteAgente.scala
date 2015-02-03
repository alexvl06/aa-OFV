package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
case class PerfilAgenteAgente(idUsuario: Int, idPerfil: Int)

class PerfilAgenteAgenteTable(tag: Tag) extends Table[PerfilUsuario](tag, "PERFILAGENTE_AGENTE") {

  def idUsuario      = column[Int]("ID_USUARIO", O.PrimaryKey)
  def idPerfil  = column[Int]("ID_PERFIL",O.PrimaryKey)

  def * =  (idUsuario, idPerfil) <> (PerfilAgenteAgente.tupled, PerfilAgenteAgente.unapply)
}
