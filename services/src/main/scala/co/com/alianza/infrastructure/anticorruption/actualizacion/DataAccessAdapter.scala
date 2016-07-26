package co.com.alianza.infrastructure.anticorruption.actualizacion

import co.com.alianza.persistence.messages.ActualizacionRequest

import scalaz.Validation
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scalaz.{ Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.dto._
import co.com.alianza.persistence.repositories.core.ActualizacionRepository
import co.com.alianza.persistence.util.DataBaseExecutionContext

object DataAccessAdapter {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext
  //Consulta datos cliente

  def consultaDatosCliente(documento: String, tipoDocumento: String) = {
    new ActualizacionRepository().consultaDatosCliente(documento, tipoDocumento) map { x => transformValidationDatosCliente(x) }
  }

  private def transformValidationDatosCliente(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[DatosCliente]] = {
    origin match {
      case zSuccess(response: String) =>
        val datos: Option[DatosCliente] = DataAccessTranslator.translateDatosCliente(response)
        val fecha = datos.get.`nvl(fdpn_fecha_ult_act,fdpn_fecha_creacion)`
        zSuccess(Some(datos.get.copy(fdpn_fecha_ult_act = fecha)))
      case zFailure(error) => zFailure(error)
    }
  }

  //Actualizar datos cliente
  def actualizarDatosCliente(datos: ActualizacionRequest) = {
    new ActualizacionRepository().actualizarCliente(datos) map { x => transformValidationActualizacion(x) }
  }

  private def transformValidationActualizacion(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[String]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(Some(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador paises

  def consultaPaises = {
    new ActualizacionRepository().listarPaises map { x => transformValidationPais(x) }
  }

  private def transformValidationPais(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[Pais]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translatePaises(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador ciudades

  def consultaCiudades(pais: Int) = {
    new ActualizacionRepository().listarCiudades(pais) map { x => transformValidationCiudad(x) }
  }

  private def transformValidationCiudad(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[Ciudad]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translateCiudades(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador tipo correo

  def consultaTipoCorreo = {
    new ActualizacionRepository().listarTipoCorreo map { x => transformValidationTipoCorreo(x) }
  }

  private def transformValidationTipoCorreo(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[TipoCorreo]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translateTiposCorreo(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador envio correspondencia

  def consultaEnviosCorrespondencia = {
    new ActualizacionRepository().listarEnvioCorrespondencia map { x => transformValidationEnvioCorrespondencia(x) }
  }

  private def transformValidationEnvioCorrespondencia(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[EnvioCorrespondencia]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translateEnviosCorrespondencia(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador ocupaciones

  def consultaOcupaciones = {
    new ActualizacionRepository().listarOcupaciones map { x => transformValidationOcupacion(x) }
  }

  private def transformValidationOcupacion(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[Ocupacion]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translateOcupaciones(response))
      case zFailure(error) => zFailure(error)
    }
  }

  //Adaptador actividades economicas

  def consultaActividadesEconomicas = {
    new ActualizacionRepository().listarActividadesEconomicas map { x => transformValidationActividadEconomica(x) }
  }

  private def transformValidationActividadEconomica(origin: Validation[PersistenceException, String]): Validation[PersistenceException, Option[List[ActividadEconomica]]] = {
    origin match {
      case zSuccess(response: String) => zSuccess(DataAccessTranslator.translateActividadesEconomicas(response))
      case zFailure(error) => zFailure(error)
    }
  }

}