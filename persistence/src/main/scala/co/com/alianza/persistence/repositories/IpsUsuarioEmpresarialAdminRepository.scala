package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.jdbc.JdbcBackend.Session
import scala.slick.lifted.TableQuery
import scala.util.Try
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

/**
 * Created by david on 12/06/14.
 */
class IpsUsuarioEmpresarialAdminRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  val ipsUsuarioEmpresarialAdmin = TableQuery[IpsUsuarioEmpresarialAdminTable]

  def obtenerIpsUsuarioEmpresarialAdmin(): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioEmpresarialAdminTry(session: Session)
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarialAdmin")
  }

  private def obtenerIpsUsuarioEmpresarialAdminTry(implicit session: Session): Try[Vector[IpsUsuario]] = Try {
    val result: Vector[IpsUsuario] = {
      ipsUsuarioEmpresarialAdmin.list.toVector
    }
    result
  }

  def obtenerIpsUsuarioEmpresarialAdmin(idUsuario : Int): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioEmpresarialAdminTry(session: Session, idUsuario)
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarialAdmin")
  }

  private def obtenerIpsUsuarioEmpresarialAdminTry(implicit session: Session, idUsuario : Int): Try[Vector[IpsUsuario]] = Try {
    val result: Vector[IpsUsuario] =  ipsUsuarioEmpresarialAdmin.filter(x => x.idUsuario === idUsuario).list.toVector
    result
  }

  private def obtenerIpUsuarioEmpresarialAdminTry(implicit session: Session, idUsuario : Int, ip:String): Try[Option[IpsUsuario]] = Try {
    val result: Option[IpsUsuario] =  ipsUsuarioEmpresarialAdmin.filter(x => x.idUsuario === idUsuario && x.ip === ip ).list.headOption
    result
  }


  def obtenerIpUsuarioEmpresarialAdmin(idUsuario : Int, ip:String): Future[Validation[PersistenceException, Option[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpUsuarioEmpresarialAdminTry(session: Session, idUsuario, ip )
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarialAdmin")
  }

  def guardar(ip:IpsUsuario): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = Try{ (ipsUsuarioEmpresarialAdmin  returning ipsUsuarioEmpresarialAdmin.map(_.ip)) += ip }
      resolveTry(resultTry, "Relaciona Ip UsuarioEmpresarialAdmin")
  }

  def eliminar(ipsUsuario:IpsUsuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = eliminarIpUsuarioEmpresarialAdminTry(session: Session, ipsUsuario.idUsuario, ipsUsuario.ip )
      resolveTry(resultTry, "Elimina Ip UsuarioEmpresarialAdmin")
  }

  private def eliminarIpUsuarioEmpresarialAdminTry(implicit session: Session, idUsuario : Int, ip:String): Try[Int] = Try {
    //val result: Option[IpsUsuarioTable#TableElementType] = ipsUsuario.filter(x => x.idUsuario === idUsuario && x.ip === ip ).list.headOption
    ipsUsuarioEmpresarialAdmin.filter(x => x.idUsuario === idUsuario && x.ip === ip ).delete
  }

}

