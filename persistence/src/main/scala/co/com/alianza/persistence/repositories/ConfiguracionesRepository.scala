package co.com.alianza.persistence.repositories

import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._


import scala.slick.lifted.TableQuery
import CustomDriver.simple._

class ConfiguracionesRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {


  val configuraciones = TableQuery[ConfiguracionesTable]

  def obtenerConfiguraciones(): Future[Validation[PersistenceException, List[Configuraciones]]] = loan {
    implicit session =>
      val resultTry = Try {
        configuraciones.list
      }
      resolveTry(resultTry, "Consulta todas las configuraciones del administrador de fiduciaria")
  }

  def obtenerConfiguracionPorLlave(llave: String): Future[Validation[PersistenceException, Option[Configuraciones]]] = loan {
    implicit session =>
      val resultTry = Try {
        configuraciones.filter(_.llave === llave).list.headOption
      }
      resolveTry(resultTry, "Consulta configuracion con llave " + llave)
  }

}
