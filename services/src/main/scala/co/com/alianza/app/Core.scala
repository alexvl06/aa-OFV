package co.com.alianza.app

import akka.actor.{ Props, ActorSystem }
import akka.cluster.Cluster

import co.com.alianza.domain.aggregates.confronta.{ConfrontaActorSupervisor}
import co.com.alianza.domain.aggregates.autenticacion._
import co.com.alianza.domain.aggregates.empresa.{ContrasenasClienteAdminActorSupervisor, AgenteEmpresarialActorSupervisor, ContrasenasAgenteEmpresarialActorSupervisor}
import co.com.alianza.domain.aggregates.usuarios.UsuariosActorSupervisor
import co.com.alianza.domain.aggregates.autoregistro.ConsultaClienteActorSupervisor
import co.com.alianza.domain.aggregates.contrasenas.ContrasenasActorSupervisor
import co.com.alianza.domain.aggregates.ips.IpsUsuarioActorSupervisor
import co.com.alianza.domain.aggregates.pin.PinActorSupervisor
import co.com.alianza.domain.aggregates.permisos.PermisoTransaccionalActorSupervisor
import co.com.alianza.util.ConfigApp

import com.typesafe.config.Config
//import co.com.alianza.domain.aggregates.fondos.FondosActorSupervisor


/**
 * Method override for the unique ActorSystem instance
 */
trait Core {
  implicit def system: ActorSystem
}

/**
 * Definition of the ActorSystem and the ExecutionContext
 */
trait BootedCore extends Core {
  import scala.concurrent.ExecutionContext

  implicit lazy val conf: Config = ConfigApp.conf

  implicit lazy val system = ActorSystem( "alianza-service" )
  implicit lazy val ex: ExecutionContext = system.dispatcher
  implicit lazy val cluster = Cluster(system)
  implicit lazy val dataAccesEx: ExecutionContext = system.dispatcher

  sys.addShutdownHook( system.shutdown( ) )
}

/**
 * Template project actors instantiation
 */
trait CoreActors { this: Core =>
  val usuariosActorSupervisor = system.actorOf( Props[ UsuariosActorSupervisor ], "UsuariosActorSupervisor" )
  val confrontaActorSupervisor = system.actorOf( Props[ ConfrontaActorSupervisor ], "confrontaActorSupervisor" )
  val consultaClienteActorSupervisor = system.actorOf( Props[ ConsultaClienteActorSupervisor ], "consultaClienteActorSupervisor" )
  val autenticacionActorSupervisor = system.actorOf( Props[ AutenticacionActorSupervisor ], "autenticacionActorSupervisor" )
  val autorizacionActorSupervisor = system.actorOf( Props[ AutorizacionActorSupervisor ], "autorizacionActorSupervisor" )
  val contrasenasActorSupervisor = system.actorOf( Props[ ContrasenasActorSupervisor ], "contrasenasActorSupervisor" )
  val contrasenasAgenteEmpresarialActorSupervisor = system.actorOf( Props[ ContrasenasAgenteEmpresarialActorSupervisor ], "contrasenasAgenteEmpresarialActorSupervisor" )
  val contrasenasClienteAdminActorSupervisor = system.actorOf( Props[ ContrasenasClienteAdminActorSupervisor ], "contrasenasClienteAdminActorSupervisor" )
  val ipsUsuarioActorSupervisor  = system.actorOf( Props[ IpsUsuarioActorSupervisor ], "ipsUsuarioActorSupervisor" )
  val pinActorSupervisor = system.actorOf( Props[ PinActorSupervisor ], "PinActorSupervisor" )
  val sesionActorSupervisor = system.actorOf( Props[ SesionActorSupervisor ], "sesionActorSupervisor" )
  val agenteEmpresarialActorSupervisor = system.actorOf( Props[ AgenteEmpresarialActorSupervisor ], "agenteEmpresarialActorSupervisor" )
  val permisoTransaccionalActorSupervisor = system.actorOf( Props[ PermisoTransaccionalActorSupervisor ], "permisoTransaccionalActorSupervisor" )
}

/**
 *
 */
object MainActors extends BootedCore with CoreActors
