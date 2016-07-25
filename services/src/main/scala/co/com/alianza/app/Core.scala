package co.com.alianza.app

import akka.actor.{ ActorSystem, Props }
import akka.cluster.Cluster
import co.com.alianza.domain.aggregates.actualizacion.ActualizacionActorSupervisor
import co.com.alianza.domain.aggregates.autenticacion._
import co.com.alianza.domain.aggregates.autoregistro.ConsultaClienteActorSupervisor
import co.com.alianza.domain.aggregates.autovalidacion.PreguntasAutovalidacionSupervisor
import co.com.alianza.domain.aggregates.confronta.ConfrontaActorSupervisor
import co.com.alianza.domain.aggregates.contrasenas.ContrasenasActorSupervisor
import co.com.alianza.domain.aggregates.empresa.{ AgenteEmpresarialActorSupervisor, ContrasenasAgenteEmpresarialActorSupervisor, ContrasenasClienteAdminActorSupervisor, HorarioEmpresaActorSupervisor }
import co.com.alianza.domain.aggregates.ips.IpsUsuarioActorSupervisor
import co.com.alianza.domain.aggregates.permisos.PermisoTransaccionalActorSupervisor
import co.com.alianza.domain.aggregates.pin.PinActorSupervisor
import co.com.alianza.domain.aggregates.usuarios.UsuariosActorSupervisor
import co.com.alianza.infrastructure.auditing.KafkaActorSupervisor
import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.config.oracle.OracleConfig
import co.com.alianza.persistence.config.pg.PGConfig
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import portal.transaccional.autenticacion.service.drivers.autenticacion.AutenticacionDriverRepository
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteDriverCoreRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionDriverRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaDriverRepository
import portal.transaccional.autenticacion.service.drivers.respuestas.RespuestaUsuarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuario.UsuarioDriverRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ClienteDAO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.daos.driver.UsuarioDAO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ RespuestaUsuarioDAO, IpUsuarioDAO, ReglaContrasenaDAO, ConfiguracionDAO }

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

  implicit lazy val system = ActorSystem("alianza-fid-auth-service")
  implicit lazy val ex: ExecutionContext = system.dispatcher
  implicit lazy val cluster = Cluster.get(system)
  implicit lazy val dataAccesEx: ExecutionContext = system.dispatcher

  sys.addShutdownHook(system.terminate())
}

/**
 * Template project actors instantiation
 */
trait CoreActors {
  this: Core with BootedCore =>
  val usuariosActorSupervisor = system.actorOf(Props[UsuariosActorSupervisor], "UsuariosActorSupervisor")
  val usuariosActor = system.actorSelection(usuariosActorSupervisor.path)
  val confrontaActorSupervisor = system.actorOf(Props[ConfrontaActorSupervisor], "confrontaActorSupervisor")
  val confrontaActor = system.actorSelection(confrontaActorSupervisor.path)
  val autenticacionActorSupervisor = system.actorOf(Props[AutenticacionActorSupervisor], "autenticacionActorSupervisor")
  val autenticacionActor = system.actorSelection(autenticacionActorSupervisor.path)
  val autenticacionUsuarioEmpresaActor = system.actorSelection(autenticacionActorSupervisor.path)
  val consultaClienteActorSupervisor = system.actorOf(Props[ConsultaClienteActorSupervisor], "consultaClienteActorSupervisor")
  val autorizacionActorSupervisor = system.actorOf(Props[AutorizacionActorSupervisor], "autorizacionActorSupervisor")
  val contrasenasActorSupervisor = system.actorOf(Props[ContrasenasActorSupervisor], "contrasenasActorSupervisor")
  val contrasenasAgenteEmpresarialActorSupervisor = system.actorOf(Props[ContrasenasAgenteEmpresarialActorSupervisor], "contrasenasAgenteEmpresarialActorSupervisor")
  val contrasenasClienteAdminActorSupervisor = system.actorOf(Props[ContrasenasClienteAdminActorSupervisor], "contrasenasClienteAdminActorSupervisor")

  val ipsUsuarioActorSupervisor = system.actorOf(Props[IpsUsuarioActorSupervisor], "ipsUsuarioActorSupervisor")

  val horarioEmpresaActorSupervisor = system.actorOf(Props[HorarioEmpresaActorSupervisor], "horarioEmpresaActorSupervisor")


  val pinActorSupervisor = system.actorOf(Props[PinActorSupervisor], "PinActorSupervisor")
  val pinActor = MainActors.system.actorSelection(pinActorSupervisor.path)
  val pinUsuarioEmpresarialAdminActor = MainActors.system.actorSelection(MainActors.pinActorSupervisor.path + "/pinUsuarioEmpresarialAdminActor")
  val pinUsuarioAgenteEmpresarialActor = MainActors.system.actorSelection(MainActors.pinActorSupervisor.path + "/pinUsuarioAgenteEmpresarialActor")

  val sesionActorSupervisor = system.actorOf(Props[SesionActorSupervisor], "sesionActorSupervisor")

  val agenteEmpresarialActorSupervisor = system.actorOf(Props[AgenteEmpresarialActorSupervisor], "agenteEmpresarialActorSupervisor")
  val agenteEmpresarialActor = MainActors.system.actorSelection(MainActors.agenteEmpresarialActorSupervisor.path)

  val permisoTransaccionalActorSupervisor = system.actorOf(Props[PermisoTransaccionalActorSupervisor], "permisoTransaccionalActorSupervisor")
  val permisoTransaccionalActor = MainActors.system.actorSelection(MainActors.permisoTransaccionalActorSupervisor.path)

  val actualizacionActorSupervisor = system.actorOf(Props[ActualizacionActorSupervisor], "actualizacionActorSupervisor")
  val actualizacionActor = system.actorSelection(actualizacionActorSupervisor.path)

  val kafkaActorSupervisor = system.actorOf(Props[KafkaActorSupervisor], "kafkaActorSupervisor")
  val kafkaActor = system.actorSelection(kafkaActorSupervisor.path)

  val preguntasAutovalidacionSupervisor = system.actorOf(Props[PreguntasAutovalidacionSupervisor], "preguntasAutovalidacionSupervisor")
  val preguntasAutovalidacionActor = system.actorSelection(preguntasAutovalidacionSupervisor.path)
}

trait Storage extends StoragePGAlianzaDB with BootedCore {
  lazy val usuarioRepo = UsuarioDriverRepository(usuarioDAO)(ex)
  lazy val clienteRepo = ClienteDriverCoreRepository(clienteDAO)(ex)
  lazy val ipUsuarioRepo = IpUsuarioDriverRepository(ipUsuarioDAO)(ex)
  lazy val configuracionRepo = ConfiguracionDriverRepository(configuracionDAO)(ex)
  lazy val reglaContrasenaRepo = ReglaContrasenaDriverRepository(reglaContrasenaDAO)(ex)
  lazy val respuestaUsuarioRepo = RespuestaUsuarioDriverRepository(respuestaUsuarioDAO)(ex)
  lazy val autenticacionRepo = AutenticacionDriverRepository(usuarioRepo, clienteRepo, configuracionRepo, reglaContrasenaRepo, ipUsuarioRepo, respuestaUsuarioRepo)(ex)
}

private[app] sealed trait StoragePGAlianzaDB extends BootedCore {
  implicit val config: DBConfig = new DBConfig with PGConfig
  implicit val configCore: DBConfig = new DBConfig with OracleConfig

  lazy val usuarioDAO = UsuarioDAO()(config)
  lazy val clienteDAO = ClienteDAO()(ex, configCore)
  lazy val ipUsuarioDAO = IpUsuarioDAO()(config)
  lazy val configuracionDAO = ConfiguracionDAO()(config)
  lazy val reglaContrasenaDAO = ReglaContrasenaDAO()(config)
  lazy val respuestaUsuarioDAO = RespuestaUsuarioDAO()(config)
}

/**
 *
 */
object MainActors extends BootedCore with CoreActors
