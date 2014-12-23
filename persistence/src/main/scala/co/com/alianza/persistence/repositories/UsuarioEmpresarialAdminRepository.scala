package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.persistence.entities._
import co.com.alianza.exceptions.PersistenceException
import CustomDriver.simple._
import scala.util.Try

/**
 * Created by manuel on 18/12/14.
 */
class UsuarioEmpresarialAdminRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository{


  val UsuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]

  def obtenerUsuarioToken( token:String ): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresarialesAdmin.filter(_.token === token).list.headOption}
      resolveTry(resultTry, "Consulta usuario empresarial admin por token: " + token)
  }

  def asociarTokenUsuario( usuarioId: Int, token: String ) : Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try{ UsuariosEmpresarialesAdmin.filter( _.id === usuarioId ).map(_.token ).update(Some(token))  }
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

}
