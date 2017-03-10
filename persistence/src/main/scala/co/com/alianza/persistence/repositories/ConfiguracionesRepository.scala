package co.com.alianza.persistence.repositories

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

class ConfiguracionesRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val configuraciones = TableQuery[ConfiguracionesTable]

  def obtenerConfiguraciones(): Future[Validation[PersistenceException, Seq[Configuracion]]] = loan {
    implicit session =>
      val resultTry = session.database.run(configuraciones.result)
      resolveTry(resultTry, "Consulta todas las configuraciones del administrador de fiduciaria")
  }

  def obtenerConfiguracionPorLlave(llave: String): Future[Validation[PersistenceException, Option[Configuracion]]] = loan {
    implicit session =>
      val resultTry = session.database.run(configuraciones.filter(_.llave === llave).result.headOption)

      resolveTry(resultTry, "Consulta configuracion con llave " + llave)
  }

}
