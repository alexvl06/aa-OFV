package co.com.alianza.domain.aggregates.permisos

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.actor.Props
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
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
import co.com.alianza.commons.enumerations.TiposCliente

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
        resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)
        context stop self
      }

    case ConsultarPermisosAgenteMessage(idAgente) =>
      val currentSender = sender
      resolveFutureValidation(DataAccessAdapter consultaPermisosAgente idAgente,
        (x: (List[co.com.alianza.infrastructure.dto.Permiso], List[co.com.alianza.infrastructure.dto.EncargoPermisos])) => PermisosRespuesta(x._1, x._2).toJson,
        currentSender)

    case ConsultarPermisosAgenteLoginMessage(agente) =>
      val currentSender = sender
      if(agente.tipoCliente == TiposCliente.agenteEmpresarial){
        val permisosFuture = DataAccessAdapter.consultaPermisosAgenteLogin(agente.id)
        resolveFutureValidation(permisosFuture,(listaPermisos: List[Int]) => PermisosLoginRespuesta(listaPermisos.contains(2), listaPermisos.contains(4), listaPermisos.contains(3), listaPermisos.contains(1)).toJson,currentSender)
      }else {
        currentSender ! JsonUtil.toJson(PermisosLoginRespuesta(true,true,true,true))
      }

  }


}