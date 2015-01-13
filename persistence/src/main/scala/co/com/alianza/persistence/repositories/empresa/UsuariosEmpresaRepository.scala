package co.com.alianza.persistence.repositories.empresa

import java.sql.Timestamp

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._


import scala.slick.lifted.TableQuery
import CustomDriver.simple._
import co.com.alianza.persistence.entities.Usuario
import scala.Some
import co.com.alianza.persistence.repositories.AlianzaRepository
import co.com.alianza.persistence.entities._

/**
 *
 * @author seven4n
 */
class UsuariosEmpresaRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  //val usuarios = Queryable[Usuario]

  val Empresas = TableQuery[EmpresaTable]
  val UsuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val UsuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val UsuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]
  val UsuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]


  def obtenerUsuariosBusqueda(correoUsuario:String, usuario:String, nombreUsuario:String, estadoUsuario:Int, idClienteAdmin: Int): Future[Validation[PersistenceException, List[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry =  Try {
        (for {
          (clienteAdministrador, agenteEmpresarial) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join UsuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join UsuariosEmpresariales on {
            case (((uea, ueae), uee), ae) => (uee.idUsuarioEmpresarial === ae.id && ueae.idEmpresa === uee.idEmpresa && uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin ) && ( if( correoUsuario != null && !correoUsuario.isEmpty ) ae.correo === correoUsuario else if( usuario != null && !usuario.isEmpty )  ae.usuario === usuario  else if( nombreUsuario != null && !nombreUsuario.isEmpty ) ae.nombreUsuario === nombreUsuario else ae.estado inSet obtenerListaEstados( estadoUsuario ) )
          }
        } yield (agenteEmpresarial)
          ).list
      }
      resolveTry(resultTry, "Consulta agentes empresariales que pertenezcan a la empresa del cliente administrador y cumpla con parametros de busqueda")
  }


  private def obtenerListaEstados( estadoUsuarioBusqueda:Int ) : List[Int] = {
    if( estadoUsuarioBusqueda == -1 )
      List( 0, 1, 2, 3 )
    else
      List( estadoUsuarioBusqueda )
  }

  def cambiarPassword (idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { UsuariosEmpresariales.filter(_.id === idUsuario).map(_.contrasena).update(Some (password)) }
      resolveTry(resultTry, "Cambiar la contraseña de usuario agente empresarial")
  }

  def actualizarEstadoUsuario( idUsuario:Int, estado:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ UsuariosEmpresariales.filter( _.id === idUsuario ).map(_.estado ).update(estado)  }
      resolveTry(resultTry, "Actualizar estado de usuario agente empresarial")
  }

}
