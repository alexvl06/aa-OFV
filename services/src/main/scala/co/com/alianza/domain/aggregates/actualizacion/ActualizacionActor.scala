package co.com.alianza.domain.aggregates.actualizacion

import akka.actor.{Actor, ActorRef, ActorLogging, Props, OneForOneStrategy}
import akka.actor.SupervisorStrategy._
import akka.routing.RoundRobinPool
import akka.pattern._
import co.com.alianza.app.AlianzaActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.actualizacion.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => UsDataAdapter}
import co.com.alianza.infrastructure.messages.ActualizacionMessagesJsonSupport._
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto.Usuario
import co.com.alianza.persistence.messages.{DatosEmpresaRequest, ActualizacionRequest}
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.BusinessLevel
import enumerations.TiposIdentificacionCore
import spray.http.StatusCodes._
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scalaz.std.AllInstances._


class ActualizacionActorSupervisor extends Actor with ActorLogging {

  val actualizacionActor = context.actorOf(Props[ActualizacionActor], "actualizacionActor")

  def receive = {
    case message: Any => actualizacionActor forward message
  }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class ActualizacionActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val _: ExecutionContext = context.dispatcher
  import co.com.alianza.util.json.MarshallableImplicits._

  def receive = {
    case message: ObtenerPaises  => obtenerPaises
    case message: ObtenerTiposCorreo  => obtenerTiposCorreo
    case message: ObtenerOcupaciones  => obtenerOcupaciones
    case message: ObtenerDatos  => obtenerDatos(message.idUsuario)
    case message: ComprobarDatos  => comprobarDatos(message.idUsuario)
    case message: ObtenerCiudades  => obtenerCiudades(message.pais)
    case message: ObtenerEnvioCorrespondencia  => obtenerEnviosCorrespondencia
    case message: ObtenerActividadesEconomicas  => obtenerActividadesEconomicas
    case message: ActualizacionMessage => actualizarDatos(message)
  }

  def obtenerPaises = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaPaises
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerCiudades(pais: Int) = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaCiudades(pais)
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerTiposCorreo = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaTipoCorreo
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerEnviosCorrespondencia = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaEnviosCorrespondencia
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerOcupaciones = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaOcupaciones
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerActividadesEconomicas = {
    val currentSender = sender()
    val futuro = DataAccessAdapter.consultaActividadesEconomicas
    resolverFuturoLista(futuro, currentSender)
  }

  def obtenerDatos(idUsuario: Int) = {
    val currentSender = sender()
    val futuro =
      (for {
        usuario <- ValidationT(UsDataAdapter.obtenerUsuarioId(idUsuario))
        datos   <- ValidationT(DataAccessAdapter.consultaDatosCliente
          (usuario.get.identificacion, TiposIdentificacionCore.getTipoIdentificacion(usuario.get.tipoIdentificacion)))
      }yield(datos)).run
    resolverFuturo(futuro, currentSender)
  }

  def comprobarDatos(idUsuario: Int) = {
    val currentSender = sender()
    val futuro =
      (for {
        usuario <- ValidationT(UsDataAdapter.obtenerUsuarioId(idUsuario))
        datos   <- ValidationT(DataAccessAdapter.consultaDatosCliente
          (usuario.get.identificacion, TiposIdentificacionCore.getTipoIdentificacion(usuario.get.tipoIdentificacion)))
      }yield(datos)).run
    futuro onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) => {
            println("actor response : ")
            println(response)
            currentSender !  ResponseMessage(OK, response.toJson)
          }
          case zFailure(error) =>  currentSender !  ResponseMessage(Conflict, error.cause.getMessage)
        }
    }
  }

  def actualizarDatos(message: ActualizacionMessage) = {
    val currentSender = sender()
    val futuro =
      (for {
        usuario <- ValidationT(UsDataAdapter.obtenerUsuarioId(message.idUsuario.get))
        result   <- ValidationT(DataAccessAdapter.actualizarDatosCliente(obtenerActualizacionRequest(usuario.get, message)))
      }yield(result)).run
    resolverFuturo(futuro, currentSender)
  }

  def resolverFuturo(futuro: Future[Validation[PersistenceException, Option[Any]]], currentSender: ActorRef) = {
    futuro onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) => currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error) =>  currentSender !  ResponseMessage(Conflict, error.cause.getMessage)
        }
    }
  }

  def resolverFuturoLista(futuro: Future[Validation[PersistenceException, Option[List[Any]]]], currentSender: ActorRef) = {
    futuro onComplete {
      case Failure(failure) => currentSender ! failure
      case Success(value) =>
        value match {
          case zSuccess(response) => currentSender !  ResponseMessage(OK, response.toJson)
          case zFailure(error) =>  currentSender !  error
        }
    }
  }

  def obtenerActualizacionRequest(usuario: Usuario, actualizacion: ActualizacionMessage): ActualizacionRequest = {
    val datosEmpRequest = new DatosEmpresaRequest(actualizacion.fdpn_ocupacion, actualizacion.datosEmp.fdpn_if_declara_renta,
      actualizacion.datosEmp.fdpn_pafd_pais, actualizacion.datosEmp.fdpn_pafd_pais_ant, actualizacion.fdpn_ciua, actualizacion.datosEmp.fdpn_nombre_emp,
      actualizacion.datosEmp.fdpn_nit_emp, actualizacion.datosEmp.fdpn_cargo, actualizacion.datosEmp.fdpn_dire_emp,
      actualizacion.datosEmp.fdpn_ciud_emp, actualizacion.datosEmp.fdpn_ciud_nombre_emp, actualizacion.datosEmp.fdpn_tele_emp,
      actualizacion.datosEmp.fdpn_if_vactivos, actualizacion.datosEmp.fdpn_if_vpasivos, actualizacion.datosEmp.fdpn_if_vpatrimonio,
      actualizacion.datosEmp.fdpn_if_vingresos, actualizacion.datosEmp.fdpn_if_vegresos, actualizacion.datosEmp.fdpn_if_vingresos_noop_mes)

    new ActualizacionRequest(usuario.identificacion, TiposIdentificacionCore.getTipoIdentificacion(usuario.tipoIdentificacion),
      actualizacion.fdpn_nombre1, actualizacion.fdpn_nombre2, actualizacion.fdpn_apell1, actualizacion.fdpn_apell2,
      actualizacion.fdpn_pais_residencia, actualizacion.fdpn_drcl_dire_res, actualizacion.fdpn_drcl_dire_ofi, actualizacion.fdpn_drcl_ciud_res,
      actualizacion.fdpn_drcl_tele_res, actualizacion.fdpn_dcfd_email, actualizacion.fdpn_dcfd_email_ant, actualizacion.fdpn_dcfd_tipo, actualizacion.fdpn_dcfd_tipo_ant,
      actualizacion.fdpn_envio_corresp, actualizacion.fdpn_telefono_movil_1, actualizacion.fdpn_pais_tel_mov_1, datosEmpRequest)
  }

}