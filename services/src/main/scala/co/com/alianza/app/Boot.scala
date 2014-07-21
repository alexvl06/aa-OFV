package co.com.alianza.app

object Boot extends App with Api with BootedCore with HostBinding  {
  import akka.io.IO
  import spray.can.Http

  IO( Http )( system ) ! Http.Bind( rootService, interface = machineIp( ), port = portNumber( args ) )
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
