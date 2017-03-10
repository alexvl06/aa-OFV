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

  def guardar(ip: IpsUsuario): Future[Validation[PersistenceException, String]] = loan {
    implicit session =>
      val resultTry = session.database.run((ipsUsuario returning ipsUsuario.map(_.ip)) += ip)
      resolveTry(resultTry, "Relaciona Ip Usuario")
  }

}

