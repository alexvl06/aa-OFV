package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.{ PerfilUsuarioTable, RecursoPerfil, RecursoPerfilTable, UsuarioTable }
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by alexandra on 25/07/16.
 */
case class AlianzaDAO()(implicit dcConfig: DBConfig) {

  val recursos = TableQuery[RecursoPerfilTable]
  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuario = TableQuery[PerfilUsuarioTable]

  import dcConfig.db._
  import dcConfig.profile.api._
}
