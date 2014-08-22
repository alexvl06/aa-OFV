package co.com.alianza.app

import akka.actor.{ Props, ActorSystem }
import co.com.alianza.domain.aggregates.contrasenas.ContrasenasActor
import co.com.alianza.domain.aggregates.ips.IpsUsuarioActor
import co.com.alianza.domain.aggregates.usuarios.UsuariosActor
import co.com.alianza.domain.aggregates.autoregistro.ConsultaClienteActor
import co.com.alianza.domain.aggregates.confronta.{ConfrontaAditionalActorSupervisor, ConfrontaValidationActorSupervisor, ConfrontaActorSupervisor}
import co.com.alianza.domain.aggregates.autenticacion.{AutorizacionActor, AutenticacionActor}
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
  implicit lazy val conf: Config = system.settings.config

  implicit lazy val system = ActorSystem( "alianza-service" )
  implicit lazy val ex: ExecutionContext = system.dispatcher
  implicit lazy val dataAccesEx: ExecutionContext = system.dispatcher

  sys.addShutdownHook( system.shutdown( ) )
}

/**
 * Template project actors instantiation
 */
trait CoreActors { this: Core =>
  val usuariosActor = system.actorOf( Props[ UsuariosActor ], "UsuariosActor" )
  val confrontaActorSupervisor = system.actorOf( Props[ ConfrontaActorSupervisor ], "confrontaActorSupervisor" )
  val confrontaValidationActorSupervisor = system.actorOf( Props[ ConfrontaValidationActorSupervisor ], "confrontaValidationActorSupervisor" )
  val confrontaAditionalActorSupervisor = system.actorOf( Props[ ConfrontaAditionalActorSupervisor ], "confrontaAditionalActorSupervisor" )
  val consultaClienteActor = system.actorOf( Props[ ConsultaClienteActor ], "consultaClienteActor" )
  val autenticacionActor = system.actorOf( Props[ AutenticacionActor ], "autenticacionActor" )
  val autorizacionActor = system.actorOf( Props[ AutorizacionActor ], "autorizacionActor" )
  val contrasenasActor = system.actorOf( Props[ ContrasenasActor ], "contrasenasActor" )
  val ipsUsuarioActor  = system.actorOf( Props[ IpsUsuarioActor ], "ipsUsuarioActor" )
}

/**
 *
 */
object MainActors extends BootedCore with CoreActors
