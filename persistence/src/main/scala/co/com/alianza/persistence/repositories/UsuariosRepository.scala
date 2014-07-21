package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities.{CustomDriver, UsuarioTable, Usuario}


import scala.slick.lifted.TableQuery
import CustomDriver.simple._
import scala.slick.direct.Queryable

/**
 *
 * @author seven4n
 */
class UsuariosRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  //val usuarios = Queryable[Usuario]

  val usuarios = TableQuery[UsuarioTable]

  def obtenerUsuarios(): Future[Validation[PersistenceException, List[Usuario]]] = loan {
    implicit session =>
      val resultTry =  Try { usuarios.list }
      resolveTry(resultTry, "Consulta todos los Usuarios")
  }

  def obtenerUsuarioNumeroIdentificacion( numeroIdentificacion:String ): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = Try{ usuarios.filter(_.identificacion === numeroIdentificacion).list.headOption}
      resolveTry(resultTry, "Consulta usuario con identificador " + numeroIdentificacion)
  }

  def obtenerUsuarioToken( token:String ): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = Try{ usuarios.filter(_.token === token).list.headOption}
      resolveTry(resultTry, "Consulta usuario con token" + token)
  }

  def obtenerUsuarioCorreo( correo:String ): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = Try{ usuarios.filter(_.correo === correo).list.headOption}
      resolveTry(resultTry, "Consulta usuario con correo " + correo)
  }

  def guardar(usuario:Usuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{  (usuarios returning usuarios.map(_.id.get)) +=usuario }
      resolveTry(resultTry, "Crea usuario")
  }

  def asociarTokenUsuario( numeroIdentificacion:String, token:String  ) : Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try{ usuarios.filter( _.identificacion === numeroIdentificacion ).map( x => ( x.token )).update(( Some(token) ))  }
      resolveTry(resultTry, "Actualizar usuario en token")
  }

  def actualizarNumeroIngresosErroneos( numeroIdentificacion:String, numeroIntentos:Int ): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try{ usuarios.filter( _.identificacion === numeroIdentificacion ).map( x => ( x.numeroIngresosErroneos )).update(( numeroIntentos ))  }
      resolveTry(resultTry, "Actualizar usuario en numeroIngresosErroneos ")
  }

  def actualizarEstadoUsuario( numeroIdentificacion:String, estado:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuarios.filter( _.identificacion === numeroIdentificacion ).map( x => ( x.estado )).update(( estado ))  }
      resolveTry(resultTry, "Actualizar estado del usuario ")
  }










}
