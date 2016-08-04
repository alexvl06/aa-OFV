package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by david on 12/06/14.
 */
// Todo : Ya se puede borrar !!! By : Alexa
class IpsUsuarioRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val ipsUsuario = TableQuery[IpsUsuarioTable]

  def obtenerIpsUsuario(): Future[Validation[PersistenceException, Seq[IpsUsuario]]] = loan {
    session =>
      val resultTry = session.database.run(ipsUsuario.result)
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  def obtenerIpsUsuario(idUsuario: Int): Future[Validation[PersistenceException, Seq[IpsUsuario]]] = loan {
    session =>
      val resultTry = session.database.run(ipsUsuario.filter(x => x.idUsuario === idUsuario).result)
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  def obtenerIpUsuario(idUsuario: Int, ip: String): Future[Validation[PersistenceException, Option[IpsUsuario]]] = loan {
    session =>
      val resultTry = session.database.run(ipsUsuario.filter(x => x.idUsuario === idUsuario && x.ip === ip).result.headOption)
      resolveTry(resultTry, "Consulta todas las IpsUsuario")
  }

  def guardar(ip: IpsUsuario): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = session.database.run((ipsUsuario returning ipsUsuario.map(_.ip)) += ip)
      resolveTry(resultTry, "Relaciona Ip Usuario")
  }

  def eliminar(ipsUsuarioE: IpsUsuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(ipsUsuario.filter(x => x.idUsuario === ipsUsuarioE.idUsuario && x.ip === ipsUsuarioE.ip).delete)
      resolveTry(resultTry, "Elimina Ip Usuario")
  }

}

