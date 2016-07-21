package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

/**
 * Created by david on 12/06/14.
 */
class IpsEmpresaRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ipsEmpresa = TableQuery[IpsEmpresaTable]

  def obtenerIpsEmpresa(idEmpresa: Int): Future[Validation[PersistenceException, Seq[IpsEmpresa]]] = loan {
    session =>
      val resultTry = session.database.run(ipsEmpresa.filter(x => x.idEmpresa === idEmpresa).result)
      resolveTry(resultTry, "Consulta todas las de una Empresa")
  }

  def guardar(ip: IpsEmpresa): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = session.database.run(ipsEmpresa returning ipsEmpresa.map(_.ip) += ip)
      resolveTry(resultTry, "Relaciona Ip Empresa")
  }

  def eliminar(IpsEmpresa: IpsEmpresa): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = ipsEmpresa.filter(x => x.idEmpresa === IpsEmpresa.idEmpresa && x.ip === IpsEmpresa.ip).delete
      val resultTry = session.database.run(query)
      resolveTry(resultTry, "Elimina Ip Empresa")
  }
}