package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.{Failure, Success, Try}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

import co.com.alianza.persistence.entities._


import scala.slick.lifted.TableQuery
import CustomDriver.simple._
import scala.slick.direct.Queryable
import scala.slick.jdbc.JdbcBackend.Session

/**
 * Created by david on 12/06/14.
 */
class IpsUsuarioRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  val ipsUsuario = TableQuery[IpsUsuarioTable]

  def obtenerIpsUsuario(): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioTry(session: Session)
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  private def obtenerIpsUsuarioTry(implicit session: Session): Try[Vector[IpsUsuario]] = Try {
    ipsUsuario.list.toVector
  }

  def obtenerIpsUsuario(idUsuario : Int): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioTry(session: Session, idUsuario)
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  private def obtenerIpsUsuarioTry(implicit session: Session, idUsuario : Int): Try[Vector[IpsUsuario]] = Try {
    val result: Vector[IpsUsuario] =  ipsUsuario.filter(x => x.idUsuario === idUsuario).list.toVector
    result
  }

  private def obtenerIpUsuarioTry(implicit session: Session, idUsuario : Int, ip:String): Try[Option[IpsUsuario]] = Try {
    val result: Option[IpsUsuario] =  ipsUsuario.filter(x => x.idUsuario === idUsuario && x.ip === ip ).list.headOption
    result
  }


  def obtenerIpUsuario(idUsuario : Int, ip:String): Future[Validation[PersistenceException, Option[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpUsuarioTry(session: Session, idUsuario, ip )
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  def guardar(ip:IpsUsuario): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = Try{ (ipsUsuario  returning ipsUsuario.map(_.ip)) += ip }
      resolveTry(resultTry, "Relaciona Ip Usuario")
  }

  def eliminar(ipsUsuario:IpsUsuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = eliminarIpUsuarioTry(session: Session, ipsUsuario.idUsuario, ipsUsuario.ip )
      resolveTry(resultTry, "Elimina Ip Usuario")
  }

  private def eliminarIpUsuarioTry(implicit session: Session, idUsuario : Int, ip:String): Try[Int] = Try {
    ipsUsuario.filter(x => x.idUsuario === idUsuario && x.ip === ip ).delete
  }

}

