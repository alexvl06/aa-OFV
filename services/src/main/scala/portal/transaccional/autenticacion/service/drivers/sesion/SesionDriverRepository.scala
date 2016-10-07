package portal.transaccional.autenticacion.service.drivers.sesion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.{ ObtenerIps, RemoverIp, ObtenerEmpresaActor, AgregarIp }
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor.SesionUsuarioCreada
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n 2016
 */
case class SesionDriverRepository(sessionActor: ActorRef)(implicit val ex: ExecutionContext) extends SesionRepository {

  implicit val timeout = Timeout(5.seconds)

  def crearSesion(token: String, inactividad: Int, empresa: Option[Empresa]): Future[SesionUsuarioCreada] = {
    (sessionActor ? CrearSesionUsuario(token, inactividad, empresa)).mapTo[SesionUsuarioCreada]
  }

  def eliminarSesion(token: String): Future[Future[Any]] = Future {
    sessionActor ? InvalidarSesion(token)
  }

  def validarSesion(token: String): Future[Boolean] = {
    sessionActor ? ValidarSesion(token) flatMap {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("403.9", "Error sesión1"))
    }
  }

  def obtenerSesion(token: String): Future[ActorRef] = {
    sessionActor ? BuscarSesion(token) flatMap {
      case Some(sesionActor: ActorRef) => Future.successful(sesionActor)
      case _ => Future.failed(ValidacionException("403.9", "Error sesión2"))
    }
  }

  def agregarIpEmpresa(idEmpresa: Int, ip: String): Future[List[String]] = {
    sessionActor ? ObtenerEmpresaSesionActorId(idEmpresa) flatMap {
      case Some(empresaActor: ActorRef) => (empresaActor ? RemoverIp(ip)).mapTo[List[String]]
      case None => Future.successful(List.empty[String])
      case _ => Future.failed(ValidacionException("403.9", "Error sesión2"))
    }
  }

  def eliminarIpEmpresa(idEmpresa: Int, ip: String): Future[List[String]] = {
    sessionActor ? ObtenerEmpresaSesionActorId(idEmpresa) flatMap {
      case Some(empresaActor: ActorRef) => (empresaActor ? AgregarIp(ip)).mapTo[List[String]]
      case None => Future.successful(List.empty[String])
      case _ => Future.failed(ValidacionException("403.9", "Error sesión2"))
    }
  }

  def obtenerIps(sesion: ActorRef): Future[List[String]] = {
    (sesion ? ObtenerEmpresaActor).flatMap {
      case Some(empresaSesionActor: ActorRef) => (empresaSesionActor ? ObtenerIps).mapTo[List[String]]
      case None => Future.failed(ValidacionException("401.21", "Error sesión"))
      case _ => Future.failed(ValidacionException("401.21", "esta devolviendo otra cosa"))
    }
  }

}
