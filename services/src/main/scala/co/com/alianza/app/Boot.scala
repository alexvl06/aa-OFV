package co.com.alianza.app

import akka.actor.Props

object Boot extends App with HostBinding  {
  import akka.io.IO
  import spray.can.Http
  val sys = MainActors.system
  implicit val _ = sys.dispatcher
  val rootService = sys.actorOf(Props(new AlianzaRouter), name = "api-AlianzaRouter")

  IO( Http )( sys ) ! Http.Bind( rootService, interface = machineIp( ), port = portNumber( args ) )
}

trait HostBinding {
	import java.net.InetAddress
	
  def portNumber( args: Array[ String ] ): Int =
    if ( args.length != 0 ) args( 0 ).toInt else 4900

  def machineIp(): String =
    "0.0.0.0"//NetworkInterface.getByName( s"eth0" ).getInetAddresses.map( matchIp ).flatten.mkString

  private def matchIp( address: InetAddress ): Option[ String ] =
    """\b(?:\d{1,3}\.){3}\d{1,3}\b""".r.findFirstIn( address.getHostAddress( ) )
}
