package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 3/02/15.
 */
case class PerfilClienteAdminClienteAdmin(idUsuario: Int, idPerfil: Int)

class PerfilClienteAdminClienteAdminTable(tag: Tag) extends Table[PerfilClienteAdminClienteAdmin](tag, "PERFILCLIADM_CLIENTEADMIN") {

  def idUsuario      = column[Int]("ID_USUARIO", O.PrimaryKey)
  def idPerfil  = column[Int]("ID_PERFIL",O.PrimaryKey)

  def * =  (idUsuario, idPerfil) <> (PerfilClienteAdminClienteAdmin.tupled, PerfilClienteAdminClienteAdmin.unapply)
}
