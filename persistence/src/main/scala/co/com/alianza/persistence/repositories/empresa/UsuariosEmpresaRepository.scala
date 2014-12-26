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


  def obtenerUsuariosBusqueda(correoUsuario:String, tipoIdentificacion:Int, numeroIdentificacion:String, estadoUsuario:Int, idClienteAdmin: Int): Future[Validation[PersistenceException, List[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry =  Try {
        (for {
          (clienteAdministrador, agenteEmpresarial) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join UsuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join UsuariosEmpresariales on {
            case (((uea, ueae), uee), ae) => uee.idUsuarioEmpresarial === ae.id && ae.identificacion === numeroIdentificacion && ae.correo === correoUsuario && ae.tipoIdentificacion === tipoIdentificacion && ae.estado === estadoUsuario
          }
        } yield (agenteEmpresarial)
          ).list
      }
      resolveTry(resultTry, "Consulta agentes empresariales que pertenezcan a la empresa del cliente administrador y cumpla con parametros de busqueda")
  }




}
