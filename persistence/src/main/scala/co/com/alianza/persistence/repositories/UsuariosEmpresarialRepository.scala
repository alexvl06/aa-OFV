package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.{CustomDriver, UsuarioEmpresarial, UsuarioEmpresarialTable, EmpresaTable, UsuarioEmpresarialEmpresaTable}
import scala.util.Try
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.persistence.entities._
import CustomDriver.simple._

/**
 * Created by manuel on 9/12/14.
 */
class UsuariosEmpresarialRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository{

  val Empresas = TableQuery[EmpresaTable]
  val UsuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val UsuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]

  def obtieneUsuarioEmpresaPorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session => resolveTry(Try {
      (
        for {
          ((usuarioEmpresarial, usuarioEmpresarialEmpresa), empresa) <-
          UsuariosEmpresariales join UsuariosEmpresarialesEmpresa on {
            (ue, uee) => ue.id===uee.idUsuarioEmpresarial
          } join Empresas on {
            case ((ue, uee), e) => e.nit===nit && ue.usuario===usuario
          }
        } yield (usuarioEmpresarial)
      ) firstOption
    }, "Consulta usuario empresarial por nit y usuario")
  }

  def asociarTokenUsuario( usuarioId: Int, token: String ) : Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === usuarioId ).map(_.token ).update(Some(token))  }
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

}
