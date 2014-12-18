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
import scala.collection.mutable.ListBuffer

/**
 *
 * @author seven4n
 */
class UsuariosEmpresaRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  //val usuarios = Queryable[Usuario]

  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuarios = TableQuery[PerfilUsuarioTable]
  val pinusuarios = TableQuery[PinUsuarioTable]


  def obtenerUsuariosBusqueda(correoUsuario:String, tipoIdentificacion:Int, numeroIdentificacion:String, estadoUsuario:Int): Future[Validation[PersistenceException, List[Usuario]]] = loan {
    implicit session =>
      val resultTry =  Try {
        val usuariosLista = new ListBuffer[Usuario]()
        //Se obtiene la lista de usuarios tipo agente empresarial que pertenecen a mi empresa





        if( correoUsuario.isEmpty && tipoIdentificacion == -1 && numeroIdentificacion.isEmpty && estadoUsuario == -1 )
          usuariosLista ++=  usuarios.list
        else{
          //Solo correo
          if( !correoUsuario.isEmpty && tipoIdentificacion == -1 && numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo y tipo
          if( !correoUsuario.isEmpty && tipoIdentificacion != -1 && numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario && x.tipoIdentificacion === tipoIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo y numero
          if( !correoUsuario.isEmpty && tipoIdentificacion == -1 && !numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario && x.identificacion === numeroIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo y estado
          if( !correoUsuario.isEmpty && tipoIdentificacion == -1 && numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo tipo y numero
          if( !correoUsuario.isEmpty && tipoIdentificacion != -1 && !numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario && x.tipoIdentificacion === tipoIdentificacion
              && x.identificacion === numeroIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo, tipo y estado
          if( !correoUsuario.isEmpty && tipoIdentificacion != -1 && numeroIdentificacion.isEmpty && estadoUsuario != -1  ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario
              && x.tipoIdentificacion === tipoIdentificacion && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Correo, numero y estado
          if( !correoUsuario.isEmpty && tipoIdentificacion == -1 && !numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario
              && x.identificacion === numeroIdentificacion && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }
          else
          //Solo tipo
          if( correoUsuario.isEmpty && tipoIdentificacion != -1 && numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.tipoIdentificacion === tipoIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Tipo y numero
          if( correoUsuario.isEmpty && tipoIdentificacion != -1 && !numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.tipoIdentificacion === tipoIdentificacion && x.identificacion === numeroIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Tipo y estado
          if( correoUsuario.isEmpty && tipoIdentificacion != -1 && numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.tipoIdentificacion === tipoIdentificacion && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Tipo numero y estado
          if( correoUsuario.isEmpty && tipoIdentificacion != -1 && !numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.tipoIdentificacion === tipoIdentificacion && x.identificacion === numeroIdentificacion
              && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Solo numero
          if( correoUsuario.isEmpty && tipoIdentificacion == -1 && !numeroIdentificacion.isEmpty && estadoUsuario == -1 ){
            val queryCorreo = usuarios.filter(x => x.identificacion === numeroIdentificacion )
            usuariosLista ++= queryCorreo.list
          }else
          //Numero y estado
          if( correoUsuario.isEmpty && tipoIdentificacion == -1 && !numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.identificacion === numeroIdentificacion && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Solo estado
          if( correoUsuario.isEmpty && tipoIdentificacion == -1 && numeroIdentificacion.isEmpty && estadoUsuario != -1 ){
            val queryCorreo = usuarios.filter(x => x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }else
          //Todos
          if( !correoUsuario.isEmpty && tipoIdentificacion != -1 && !numeroIdentificacion.isEmpty && estadoUsuario != -1  ){
            val queryCorreo = usuarios.filter(x => x.correo === correoUsuario
              && x.tipoIdentificacion === tipoIdentificacion && x.identificacion === numeroIdentificacion
              && x.estado === estadoUsuario )
            usuariosLista ++= queryCorreo.list
          }
        }
        usuariosLista.toList
      }
      resolveTry(resultTry, "Consulta todos los Usuarios")
  }




}
