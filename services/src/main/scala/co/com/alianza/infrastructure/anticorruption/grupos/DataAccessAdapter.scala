package co.com.alianza.infrastructure.anticorruption.grupos

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.repositories.core.ClienteRepository

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.{ Failure => zFailure, Success => zSuccess, Validation }

object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx

  def consultarGrupo(idGrupo: Int): Future[Validation[PersistenceException, Option[Cliente]]] = {
    val repo = new ClienteRepository()
    repo consultaGrupo idGrupo map { x => transformValidationMockGrupo(x) }
  }

  private def transformValidationMockGrupo(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[Cliente]] = {
    origin match {
      case zSuccess(response: String) =>
        val jsonMock: String = "[{\n\"wcli_nombre\": \"" + response + "\",\n\"wcli_person\": \"G\",\n\"wcli_estado\": \"AC\",\n\"wcli_estado_descri\": \"Activo\",\n\"wcli_dir_correo\": \"grupo@mock.com\",\n\"wcli_ident_replegal\": \"666\",\n\"wcli_cias_pagos_masivos\": \"\"\n}]"
        zSuccess(DataAccessTranslator.translateCliente(jsonMock))
      case zFailure(error) => zFailure(error)
    }
  }

}

