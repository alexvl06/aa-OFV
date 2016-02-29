package co.com.alianza.domain.aggregates.permisos

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.actor.Props
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.errores.{ErrorPasswordInvalido, ErrorAutenticacion, ErrorClienteNoExisteCore, ErrorPersistencia}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.util.clave.ErrorUltimasContrasenas

import scala.util.{Success, Failure}

//import co.com.alianza.infrastructure.anticorruption.clientes.DataAccessAdapter
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scalaz.Validation
import spray.http.StatusCodes._
import scalaz.std.AllInstances._
import co.com.alianza.infrastructure.messages._
import co.com.alianza.infrastructure.dto._
import co.com.alianza.util.FutureResponse
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.anticorruption.permisos.{PermisoTransaccionalDataAccessAdapter => DataAccessAdapter}
import co.com.alianza.util.json.MarshallableImplicits._
import scalaz.{Success => zSuccess, Failure => zFailure}
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.commons.enumerations.{PermisosFideicomisosCoreAlianza, TiposCliente}
import co.com.alianza.infrastructure.anticorruption.clientes.{DataAccessAdapter => ClDataAdapter}

/**
 * Created by manuel on 8/01/15.
 */
class PermisoTransaccionalActorSupervisor extends Actor with ActorLogging {

  def receive = { case message: MessageService =>  context actorOf(Props[PermisoTransaccionalActor]) forward message }

  override val supervisorStrategy = OneForOneStrategy() {
    case exception: Exception =>
      exception.printStackTrace()
      log.error(exception, exception.getMessage)
      Restart
  }

}

class PermisoTransaccionalActor extends Actor with ActorLogging with FutureResponse {

  implicit val _: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 120 seconds

  var numeroPermisos = 0
  
  case class RestaVerificacionMessage(currentSender: ActorRef)

  def receive = {
    case GuardarPermisosAgenteMessage(idAgente, permisosGenerales, encargosPermisos, idClienteAdmin) =>
      val currentSender = sender
      val permisosEncargos = encargosPermisos flatMap {e => e.permisos.map(p => p.copy(permiso = p.permiso.map{_.copy(idEncargo = e.wspf_plan, idAgente = idAgente)}))}
      numeroPermisos = permisosGenerales.length + permisosEncargos.length
      if(numeroPermisos==0)
        self ! RestaVerificacionMessage(currentSender)
      else {
        permisosGenerales foreach { p => self ! ((p, idClienteAdmin, currentSender): (Permiso, Option[Int], ActorRef)) }
        permisosEncargos foreach { p => self ! ((p, idClienteAdmin, currentSender): (PermisoTransaccionalUsuarioEmpresarialAgentes, Option[Int], ActorRef)) }
      }

    case (permiso: Permiso, idClienteAdmin: Option[Int], currentSender: ActorRef) =>
      val future = (for {
        result <- ValidationT(DataAccessAdapter guardaPermiso (permiso.permisoAgente.get, permiso.autorizadores.map(_.map(_.id)), idClienteAdmin.get))
      } yield result).run
      resolveFutureValidation(future, (x: Int) => RestaVerificacionMessage(currentSender), self)

    case (permisoAgentes: PermisoTransaccionalUsuarioEmpresarialAgentes, idClienteAdmin: Option[Int], currentSender: ActorRef) =>
      val future = (for {
        result <- ValidationT(DataAccessAdapter guardaPermisoEncargo (permisoAgentes.permiso.get, permisoAgentes.agentes.map(_.map(_.id)), idClienteAdmin.get))
      } yield result).run
      resolveFutureValidation(future, (x: Int) => RestaVerificacionMessage(currentSender), self)

    case RestaVerificacionMessage(currentSender) =>
      numeroPermisos -= 1
      if (numeroPermisos<=0) {
        val future = (for {
          result <- ValidationT(Future.successful(Validation.success(ResponseMessage(OK, "Guardado de permisos correcto"))))
        } yield result).run
        resolveFutureValidation(future, { (x: ResponseMessage) => context stop self; x }, currentSender)
      }

    case ConsultarPermisosAgenteMessage(idAgente) =>
      val currentSender = sender
      resolveFutureValidation (
        DataAccessAdapter consultaPermisosAgente idAgente,
        {(x: (List[co.com.alianza.infrastructure.dto.Permiso], List[co.com.alianza.infrastructure.dto.EncargoPermisos])) =>
          context stop self
          PermisosRespuesta(x._1, x._2) toJson
        },
        currentSender
      )

    case ConsultarPermisosAgenteLoginMessage(agente, identificacionUsuario) =>
      val currentSender = sender
      val tienePermisosPagosMasivosFidCore = verificarPermisosCore( identificacionUsuario )
      if(agente.tipoCliente == TiposCliente.agenteEmpresarial){
        val permisosFuture = DataAccessAdapter.consultaPermisosAgenteLogin(agente.id)
        resolveFutureValidation(permisosFuture,
          {(listaPermisos: List[Int]) =>
            context stop self
            PermisosLoginRespuesta(listaPermisos.contains(2), listaPermisos.contains(4), listaPermisos.contains(3), listaPermisos.contains(1), listaPermisos.contains(6), listaPermisos.contains(7), tienePermisosPagosMasivosFidCore).toJson
          },
          currentSender)
      } else {
        currentSender ! JsonUtil.toJson(PermisosLoginRespuesta(true,true,true,true,true,true, tienePermisosPagosMasivosFidCore))
        context stop self
      }

  }

  /**
    * Valida que el usuario exista en el core de alianza
    * @param identificacionUsuario numero de identificacion del usuario
    * @return Future[Validation[ErrorAutenticacion, Cliente] ]
    * Success => Cliente
    * ErrorAutenticacion => ErrorPersistencia | ErrorClienteNoExisteCore
    */
  def obtenerClienteSP(identificacionUsuario: String): Future[Validation[ErrorAutenticacion, Cliente]] = {
    log.info("Validando que el cliente exista en el core de alianza")
    val future: Future[Validation[PersistenceException, Option[Cliente]]] = ClDataAdapter.consultarCliente(identificacionUsuario)
    future.map(_.leftMap(pe => ErrorPersistencia(pe.message, pe)).flatMap {
      case Some(cliente) => Validation.success(cliente)
      case None => Validation.failure(ErrorClienteNoExisteCore())
    })
  }

  private def verificarPermisosCore(numeroIdentificacion:String): Boolean ={
    //Se consulta los permisos del core sobre el cliente fid
    val cliente: Future[Validation[ErrorAutenticacion, Cliente]] = (for {
      cliente           <- ValidationT(obtenerClienteSP(numeroIdentificacion))

    } yield cliente).run
    val extraccionFuturo = Await.result( cliente, 8 seconds )
    extraccionFuturo match {
      case zSuccess(cliente) =>
        cliente.wcli_cias_pagos_masivos == PermisosFideicomisosCoreAlianza.`SI`.nombre
      case zFailure(error) => false
    }

  }


}