package co.com.alianza.domain.aggregates.autenticacion

import akka.actor._
import akka.pattern.ask
import akka.cluster.{Member, MemberStatus}
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.collection.immutable.SortedSet
import scala.collection.immutable.Vector
import scala.util.{Failure, Success}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

import co.com.alianza.persistence.entities.{IpsEmpresa, IpsUsuario}
import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usuarioAdaptador}
import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages._

/**
 * Created by manuel on 3/03/15.
 */
class EmpresaActor(var empresa: Empresa) extends Actor with ActorLogging {

  implicit val _: ExecutionContext = context dispatcher
  implicit val timeout: Timeout = 120 seconds
  var sesionesActivas = List[ActorRef]()
  var ips = List[String]()

  def receive = {
    case ActualizarEmpresa(empresa) => this.empresa = empresa

    case AgregarSesion(sesion) =>
      sesionesActivas = if(!sesionesActivas.contains(sesion)) List(sesion) ::: sesionesActivas else sesionesActivas

    case AgregarIp(ip) => ips = if(!ips.contains(ip)) List(ip) ::: ips else ips

    case RemoverIp(ip) => ips = if(ips.contains(ip)) ips filterNot{_==ip} else ips

    case CerrarSesiones() => {
      sesionesActivas foreach { _ ! ExpirarSesion }
      context.stop(self)
    }

    case CargarIps() => cargaIpsEmpresa()

    case ObtenerIps() =>
      val currentSender = sender
      self ? CargarIps() onComplete {
        case Success(true) => currentSender ! ips
        case Success(false) => log error "*++*+ Falló la carga de ips"
        case Failure(error) => log error ("+++ Falló la carga de ips de la empresa", error)
      }

  }

  private def cargaIpsEmpresa() = {
    val currentSender = sender
    if(ips.isEmpty)
      usuarioAdaptador obtenerIpsEmpresa empresa.id onComplete {
        case Failure(failure) =>
          log error (failure.getMessage, failure); currentSender ! false
        case Success(value)    =>
          value match {
            case zSuccess(response: Vector[IpsEmpresa]) => ips = response map (_.ip) toList; currentSender ! true
            case zFailure(error) => log error (error.message, error); currentSender ! false
          }
    } else currentSender ! true
  }

}

object EmpresaActor {
  def props(empresa: Empresa) = Props(new EmpresaActor(empresa))
}

class BuscadorActorCluster(nombreActorPadre: String) extends Actor {

  var numResp = 0
  var resp: Option[ActorRef] = None
  val nodosBusqueda: SortedSet[Member] = ClusterUtil.obtenerNodos
  var interesado: ActorRef = null

  def receive: Receive = {
    case BuscarActor(actorName) =>
      interesado = sender;
      nodosBusqueda foreach { member =>
        this.context.actorSelection(RootActorPath(member.address) / "user" / nombreActorPadre) ! EncontrarActor(actorName)
      }
    case ActorEncontrado(actorRef) =>
      numResp += 1
      resp = Some(actorRef)
      replyIfReady()
    case _ =>
      numResp += 1
      replyIfReady()
  }

  def replyIfReady() = if(numResp == nodosBusqueda.size) { interesado ! resp; this.context.stop(self) }

}

object ClusterUtil {
  def obtenerNodos = {
    val nodosUnreach: Set[Member] = MainActors.cluster.state.unreachable // Lista de nodos en estado unreachable
    val nodosUp: SortedSet[Member] = MainActors.cluster.state.members.filter(_.status == MemberStatus.up) // Lista de nodos en estado UP
    nodosUp.filter(m => !nodosUnreach.contains(m)) // Lista de nodos que estan en estado UP y no son estan unreachable
  }
}

case class BuscarActor(actorName: String)
case class ActorEncontrado(session: ActorRef)
case object ActorNoEncontrado
case class EncontrarActor(actorName: String)
case class AgregarSesion(sesion: ActorRef)

case class AgregarIp(ip: String)

case class RemoverIp(ip: String)

case class ObtenerIps()

case class CargarIps()

