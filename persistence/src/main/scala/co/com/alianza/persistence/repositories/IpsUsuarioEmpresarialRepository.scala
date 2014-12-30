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
class IpsUsuarioEmpresarialRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository  {

  val ipsUsuarioEmpresarial = TableQuery[IpsUsuarioEmpresarialTable]

  def obtenerIpsUsuarioEmpresarial(): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioEmpresarialTry(session: Session)
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarialAdmin")
  }

  private def obtenerIpsUsuarioEmpresarialTry(implicit session: Session): Try[Vector[IpsUsuario]] = Try {
    val result: Vector[IpsUsuario] = {
      ipsUsuarioEmpresarial.list.toVector
    }
    result
  }

  def obtenerIpsUsuarioEmpresarial(idUsuario : Int): Future[Validation[PersistenceException, Vector[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpsUsuarioEmpresarialTry(session: Session, idUsuario)
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarial")
  }

  private def obtenerIpsUsuarioEmpresarialTry(implicit session: Session, idUsuario : Int): Try[Vector[IpsUsuario]] = Try {
    val result: Vector[IpsUsuario] =  ipsUsuarioEmpresarial.filter(x => x.idUsuario === idUsuario).list.toVector
    result
  }

  private def obtenerIpUsuarioEmpresarialTry(implicit session: Session, idUsuario : Int, ip:String): Try[Option[IpsUsuario]] = Try {
    val result: Option[IpsUsuario] =  ipsUsuarioEmpresarial.filter(x => x.idUsuario === idUsuario && x.ip === ip ).list.headOption
    result
  }


  def obtenerIpUsuarioEmpresarial(idUsuario : Int, ip:String): Future[Validation[PersistenceException, Option[IpsUsuario]]] = loan {
    session =>
      val resultTry = obtenerIpUsuarioEmpresarialTry(session: Session, idUsuario, ip )
      resolveTry(resultTry, "Consulta todas las IpsUsuarioEmpresarial")
  }

  def guardar(ip:IpsUsuario): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = Try{ (ipsUsuarioEmpresarial  returning ipsUsuarioEmpresarial.map(_.ip)) += ip }
      resolveTry(resultTry, "Relaciona Ip UsuarioEmpresarial")
  }

  def eliminar(ipsUsuario:IpsUsuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = eliminarIpUsuarioEmpresarialTry(session: Session, ipsUsuario.idUsuario, ipsUsuario.ip )
      resolveTry(resultTry, "Elimina Ip UsuarioEmpresarial")
  }

  private def eliminarIpUsuarioEmpresarialTry(implicit session: Session, idUsuario : Int, ip:String): Try[Int] = Try {
    //val result: Option[IpsUsuarioTable#TableElementType] = ipsUsuario.filter(x => x.idUsuario === idUsuario && x.ip === ip ).list.headOption
    ipsUsuarioEmpresarial.filter(x => x.idUsuario === idUsuario && x.ip === ip ).delete
  }

}

