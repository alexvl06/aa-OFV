package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._

import scala.concurrent.{ ExecutionContext, Future }
import slick.jdbc.JdbcBackend.Session
import slick.lifted.TableQuery
import scala.util.Try
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 12/06/14.
 */
class IpsEmpresaRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ipsEmpresa = TableQuery[IpsEmpresaTable]

  def obtenerIpsEmpresa(idEmpresa: Int): Future[Validation[PersistenceException, Vector[IpsEmpresa]]] = loan {
    session =>
      val resultTry = obtenerIpsEmpresaTry(session: Session, idEmpresa)
      resolveTry(resultTry, "Consulta todas las de una Empresa")
  }

  private def obtenerIpsEmpresaTry(implicit session: Session, idEmpresa: Int): Try[Vector[IpsEmpresa]] = Try {
    val result: Vector[IpsEmpresa] = ipsEmpresa.filter(x => x.idEmpresa === idEmpresa).list.toVector
    result
  }

  def guardar(ip: IpsEmpresa): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = Try { (ipsEmpresa returning ipsEmpresa.map(_.ip)) += ip }
      resolveTry(resultTry, "Relaciona Ip Empresa")
  }

  def eliminar(IpsEmpresa: IpsEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = eliminarIpEmpresaTry(session: Session, IpsEmpresa.idEmpresa, IpsEmpresa.ip)
      resolveTry(resultTry, "Elimina Ip Empresa")
  }

  private def eliminarIpEmpresaTry(implicit session: Session, idEmpresa: Int, ip: String): Try[Int] = Try {
    ipsEmpresa.filter(x => x.idEmpresa === idEmpresa && x.ip === ip).delete
  }

}