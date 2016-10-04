package co.com.alianza.app

import akka.actor.{ ActorRef, Props }
import akka.io.IO
import portal.transaccional.autenticacion.service.drivers.autorizacion.AutorizacionUsuarioComercialRepository
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.util.SesionAgenteUtilRepository
import spray.can.Http

object Boot extends App with HostBinding with Core with BootedCore with CoreActors with Storage {

  val sessionActor: ActorRef = sesionActorSupervisor

  private val rootService = system.actorOf(
    Props(AlianzaRouter(autenticacionRepo, autenticacionEmpresaRepo, autenticacionComercialRepo,
      usuarioRepo, usuarioAgenteRepo, usuarioAdminRepo, autorizacionUsuarioRepo, kafkaActor, preguntasAutovalidacionActor, usuariosActor,
      confrontaActor, actualizacionActor, permisoTransaccionalActor, agenteEmpresarialActor, pinActor, pinUsuarioEmpresarialAdminActor,
      pinUsuarioAgenteEmpresarialActor, ipsUsuarioActor, horarioEmpresaActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor,
      contrasenasActor, autorizacionActorSupervisor, autorizacionAgenteRepo, autorizacionAdminRepo, preguntasValidacionRepository,
      respuestaUsuarioRepo, respuestaUsuariAdminoRepo, ipRepo, autorizacionComercialRepo, autorizacionComercialAdminRepo,
      autorizacionRecursoComercialRepository, recursoComercialRepository, rolComercialRepository, agenteInmobRepo, permisoAgenteInmob,
      sesionUtilAgenteEmpresarial, sesionUtilAgenteInmobiliario, agenteInmobContrasenaRepo)),
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