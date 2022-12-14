package co.com.alianza.app

import akka.actor.{ ActorRef, Props }
import akka.io.IO
import spray.can.Http

object Boot extends App with HostBinding with Core with BootedCore with CoreActors with Storage {

  val sessionActor: ActorRef = sesionActorSupervisor

  private val rootService = system.actorOf(
    Props(AlianzaRouter(autenticacionRepo, autenticacionEmpresaRepo, autenticacionComercialRepo, autenticacionUsuarioRepository, usuarioRepo, usuarioAgenteRepo, usuarioAdminRepo,
      autorizacionUsuarioRepo, kafkaActor, usuariosActor, confrontaActor, actualizacionRepository, permisoTransaccionalActor, agenteEmpresarialActor,
      pinRepository, contrasenaAgenteRepo, contrasenaAdminRepo, contrasenaUsuarioRepo, autorizacionAgenteRepo, autorizacionAdminRepo,
      preguntasValidacionRepository, respuestaUsuarioRepo, respuestaUsuariAdminoRepo, ipRepo, autorizacionComercialRepo, autorizacionComercialAdminRepo,
      autorizacionRecursoComercialRepository, recursoComercialRepository, rolComercialRepository, usuarioComercialAdminRepo, reglaContrasenaRepo,
      horarioEmpresaRepository, agenteInmobRepo, permisoAgenteInmob,
      sesionUtilAgenteInmobiliario, agenteInmobContrasenaRepo, pinAgenteInmobRepository, autorizacionAgenteInmob, menuUsuario, autorizacionOFVRepository)),
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
    """\b(?:\d{1,3}\.){3}\d{1,3}\b""".r.findFirstIn(address.getHostAddress)
}