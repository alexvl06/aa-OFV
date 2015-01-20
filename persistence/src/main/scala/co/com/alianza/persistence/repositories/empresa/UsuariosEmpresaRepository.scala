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
import scala.collection.mutable.ListBuffer

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
        var usuariosQuery = (for {
          (clienteAdministrador, agenteEmpresarial) <-
          UsuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join UsuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join UsuariosEmpresariales on {
              case (((uea, ueae), uee), ae) =>
                uee.idUsuarioEmpresarial === ae.id && ueae.idEmpresa === uee.idEmpresa && uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin && ( ae.estado inSet obtenerListaEstados( estadoUsuario ) )
          }
        } yield (agenteEmpresarial)
          )

        val usuariosEmpresarialesLista = new ListBuffer[UsuarioEmpresarial]()
        if( (correoUsuario == null || correoUsuario.isEmpty) && ( usuario == null || usuario.isEmpty ) && ( nombreUsuario== null || nombreUsuario.isEmpty ) )
          usuariosEmpresarialesLista ++= usuariosQuery.list
        else {
          if (correoUsuario != null && !correoUsuario.isEmpty && usuariosQuery != null ) {
            if( usuariosQuery.filter(_.correo === correoUsuario).exists.run )
              usuariosQuery = usuariosQuery.filter(_.correo === correoUsuario)
            else
              usuariosQuery = null
          }
          if (usuario != null && !usuario.isEmpty && usuariosQuery != null) {
            if( usuariosQuery.filter(_.usuario === usuario).exists.run )
              usuariosQuery = usuariosQuery.filter(_.usuario === usuario)
            else
              usuariosQuery = null
          }
          if (nombreUsuario != null && !nombreUsuario.isEmpty && usuariosQuery != null) {
            if( usuariosQuery.filter(_.nombreUsuario === nombreUsuario).exists.run )
              usuariosQuery = usuariosQuery.filter(_.nombreUsuario === nombreUsuario)
            else
              usuariosQuery = null
          }
          if( usuariosQuery != null )
            usuariosEmpresarialesLista ++= usuariosQuery.list
        }
        usuariosEmpresarialesLista.toList
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
