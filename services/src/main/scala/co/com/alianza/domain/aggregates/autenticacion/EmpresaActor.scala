package co.com.alianza.domain.aggregates.autenticacion

import akka.actor._
import akka.pattern.ask
import akka.cluster.{Member, MemberStatus}
import akka.util.Timeout
import scala.collection.immutable.SortedSet

import co.com.alianza.app.MainActors
import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages._

/**
 * Created by manuel on 3/03/15.
 */
class EmpresaActor(var empresa: Empresa) extends Actor with ActorLogging {

  var sesionesActivas = List[ActorRef]()
  var ips = List[String]()

  def receive = {
    case ActualizarEmpresa(empresa) => this.empresa = empresa
    case AgregarSesion(sesion) =>
      sesionesActivas = if(!sesionesActivas.contains(sesion)) List(sesion) ::: sesionesActivas else sesionesActivas //TODO: Optimizar
    case AgregarIp(ip) => ips = if(!ips.contains(ip)) List(ip) ::: ips else ips //TODO: Optimizar
    case RemoverIp(ip) => ips = if(ips.contains(ip)) ips filterNot{_==ip} else ips //TODO: Optimizar
    case CerrarSesiones() => {
      sesionesActivas foreach { _ ! ExpirarSesion }
      context.stop(self)
    }
    case ObtenerIps() => sender ! ips
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
case class ActorNoEncontrado()
case class EncontrarActor(actorName: String)