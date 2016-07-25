package co.com.alianza.app

import akka.actor.{ ActorSelection, Props }
import akka.io.IO
import portal.transaccional.autenticacion.service.drivers.autenticacion.AutenticacionRepository
import spray.can.Http

object Boot extends App with HostBinding with Core with BootedCore with CoreActors with Storage {

  implicit val ec = system.dispatcher
  private val rootService = system.actorOf(
    Props(AlianzaRouter(autenticacionRepo, kafkaActor, preguntasAutovalidacionActor,
    usuariosActor, confrontaActor, autenticacionActor, autenticacionUsuarioEmpresaActor,
    actualizacionActor, permisoTransaccionalActor, agenteEmpresarialActor,
    pinActor, pinUsuarioEmpresarialAdminActor, pinUsuarioAgenteEmpresarialActor)),
    name = "api-AlianzaRouter"
  )

  IO(Http)(system) ! Http.Bind(rootService, interface = machineIp(), port = portNumber(args))
}

trait HostBinding {
  import java.net.InetAddress

  def portNumber(args: Array[String]): Int =
    if (args.length != 0) args(0).toInt else 4900

  def machineIp(): String =
    "0.0.0.0" //NetworkInterface.getByName( s"eth0" ).getInetAddresses.map( matchIp ).flatten.mkString

  private def matchIp(address: InetAddress): Option[String] =
    """\b(?:\d{1,3}\.){3}\d{1,3}\b""".r.findFirstIn(address.getHostAddress())
}