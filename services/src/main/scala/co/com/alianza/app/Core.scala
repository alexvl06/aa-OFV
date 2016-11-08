package co.com.alianza.app

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.Cluster
import co.com.alianza.domain.aggregates.autenticacion._
import co.com.alianza.domain.aggregates.autoregistro.ConsultaClienteActorSupervisor
import co.com.alianza.domain.aggregates.confronta.ConfrontaActorSupervisor
import co.com.alianza.domain.aggregates.contrasenas.ContrasenasActorSupervisor
import co.com.alianza.domain.aggregates.empresa.{ AgenteEmpresarialActorSupervisor, ContrasenasAgenteEmpresarialActorSupervisor, ContrasenasClienteAdminActorSupervisor }
import co.com.alianza.domain.aggregates.permisos.PermisoTransaccionalActorSupervisor
import co.com.alianza.domain.aggregates.usuarios.UsuariosActorSupervisor
import co.com.alianza.infrastructure.auditing.KafkaActorSupervisor
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import portal.transaccional.autenticacion.service.drivers.actualizacion.ActualizacionDriverRepository
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialDriverRepository, AutenticacionDriverRepository, AutenticacionEmpresaDriverRepository }
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteDriverCoreRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionDriverRepository
import portal.transaccional.autenticacion.service.drivers.empresa.EmpresaDriverRepository
import portal.transaccional.autenticacion.service.drivers.horarioEmpresa.HorarioEmpresaDriverRepository
import portal.transaccional.autenticacion.service.drivers.ip.IpDriverRepository
import portal.transaccional.autenticacion.service.drivers.ipempresa.IpEmpresaDriverRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.ldap.LdapDriverRepository
import portal.transaccional.autenticacion.service.drivers.pin.PinDriverRepository
import portal.transaccional.autenticacion.service.drivers.pregunta.{ PreguntasAutovalidacionDriverRepository, PreguntasDriverRepository }
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoDriverRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaDriverRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.{ RespuestaUsuarioAdminDriverRepository, RespuestaUsuarioDriverRepository }
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{ RecursoComercialDriverRepository, RolComercialDriverRepository, RolRecursoComercialDriverRepository }
import portal.transaccional.autenticacion.service.drivers.sesion.SesionDriverRepository
import portal.transaccional.autenticacion.service.drivers.ultimaContrasena.UltimaContrasenaDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioAgenteDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.{ UsuarioComercialAdminDriverRepository, UsuarioComercialAdminRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioDriverRepository
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.config.pg.{ OracleConfig, PGConfig }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.{ ActualizacionDAO, ClienteDAO }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.ldap.AlianzaLdapDAO
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal._

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

  sys.addShutdownHook(system.terminate())
}

/**
 * Template project actors instantiation
 */
trait CoreActors {
  this: Core with BootedCore =>
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val usuariosActorSupervisor = system.actorOf(Props[UsuariosActorSupervisor], "UsuariosActorSupervisor")
  val usuariosActor = system.actorSelection(usuariosActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val confrontaActorSupervisor = system.actorOf(Props[ConfrontaActorSupervisor], "confrontaActorSupervisor")
  val confrontaActor = system.actorSelection(confrontaActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val consultaClienteActorSupervisor = system.actorOf(Props[ConsultaClienteActorSupervisor], "consultaClienteActorSupervisor")
  val consultaClienteActor = system.actorSelection(consultaClienteActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val contrasenasActorSupervisor = system.actorOf(Props[ContrasenasActorSupervisor], "contrasenasActorSupervisor")
  val contrasenasActor = system.actorSelection(contrasenasActorSupervisor.path)
  val contrasenasAgenteEmpresarialActorSupervisor =
    system.actorOf(Props[ContrasenasAgenteEmpresarialActorSupervisor], "contrasenasAgenteEmpresarialActorSupervisor")
  val contrasenasAgenteEmpresarialActor = system.actorSelection(contrasenasAgenteEmpresarialActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val contrasenasClienteAdminActorSupervisor = system.actorOf(Props[ContrasenasClienteAdminActorSupervisor], "contrasenasClienteAdminActorSupervisor")
  val contrasenasClienteAdminActor = system.actorSelection(contrasenasClienteAdminActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val sesionActorSupervisor = system.actorOf(Props[SesionActorSupervisor], "sesionActorSupervisor")
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val agenteEmpresarialActorSupervisor = system.actorOf(Props[AgenteEmpresarialActorSupervisor], "agenteEmpresarialActorSupervisor")
  val agenteEmpresarialActor = system.actorSelection(agenteEmpresarialActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val permisoTransaccionalActorSupervisor = system.actorOf(Props[PermisoTransaccionalActorSupervisor], "permisoTransaccionalActorSupervisor")
  val permisoTransaccionalActor = system.actorSelection(permisoTransaccionalActorSupervisor.path)
  //TODO: verificar que usuarios ya no se están utilizando y eliminarlos
  val kafkaActorSupervisor = system.actorOf(Props[KafkaActorSupervisor], "kafkaActorSupervisor")
  val kafkaActor = system.actorSelection(kafkaActorSupervisor.path)
}

trait Storage extends StoragePGAlianzaDB with BootedCore {

  val sessionActor: ActorRef

  lazy val sesionRepo = SesionDriverRepository(sessionActor)
  lazy val ldapRepo = LdapDriverRepository(alianzaLdapDAO)
  lazy val recursoRepo = RecursoDriverRepository(alianzaDAO)
  lazy val empresaRepo = EmpresaDriverRepository(empresaDAO)
  lazy val usuarioRepo = UsuarioDriverRepository(usuarioDAO)
  lazy val clienteRepo = ClienteDriverCoreRepository(clienteDAO)
  lazy val ipUsuarioRepo = IpUsuarioDriverRepository(ipUsuarioDAO)
  lazy val ipEmpresaRepo = IpEmpresaDriverRepository(ipEmpresaDAO)
  lazy val configuracionRepo = ConfiguracionDriverRepository(configuracionDAO)
  lazy val ultimaContrasenaRepo = UltimaContrasenaDriverRepository(ultimaContrasenaDAO, ultimaContrasenaAdminDAO, ultimaContrasenaAgenteDAO)
  lazy val reglaContrasenaRepo = ReglaContrasenaDriverRepository(reglaContrasenaDAO, ultimaContrasenaRepo)
  lazy val usuarioAgenteRepo = UsuarioAgenteDriverRepository(usuarioAgenteDAO)
  lazy val usuarioComercialRepo = UsuarioComercialDriverRepository(usuarioComercialDAO)
  lazy val usuarioComercialAdminRepo = UsuarioComercialAdminDriverRepository(usuarioComercialAdminDAO, empresaRepo)
  lazy val respuestaUsuarioRepo = RespuestaUsuarioDriverRepository(respuestaUsuarioDAO, configuracionRepo)
  lazy val usuarioAdminRepo = UsuarioAdminDriverRepository(usuarioAdminDAO)
  lazy val respuestaUsuariAdminoRepo = RespuestaUsuarioAdminDriverRepository(respuestaUsuarioAdminDAO, configuracionRepo)
  lazy val autorizacionUsuarioRepo = AutorizacionUsuarioDriverRepository(usuarioRepo, recursoRepo, sesionRepo)
  lazy val autorizacionComercialRepo = AutorizacionUsuarioComercialDriverRepository(sesionRepo, recursoRepo, usuarioComercialRepo)
  lazy val autorizacionComercialAdminRepo = AutorizacionUsuarioComercialAdminDriverRepository(usuarioComercialAdminRepo)
  lazy val autenticacionRepo = AutenticacionDriverRepository(usuarioRepo, clienteRepo, configuracionRepo, reglaContrasenaRepo, ipUsuarioRepo,
    respuestaUsuarioRepo, sesionRepo)
  lazy val autenticacionEmpresaRepo = AutenticacionEmpresaDriverRepository(usuarioAgenteRepo, usuarioAdminRepo, clienteRepo, empresaRepo, reglaContrasenaRepo,
    configuracionRepo, ipEmpresaRepo, sesionRepo, respuestaUsuariAdminoRepo)
  lazy val autenticacionComercialRepo = AutenticacionComercialDriverRepository(ldapRepo, usuarioComercialRepo,
    usuarioComercialAdminRepo: UsuarioComercialAdminRepository, configuracionRepo, sesionRepo)
  lazy val autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialDriverRepository = AutorizacionUsuarioEmpresarialDriverRepository(
    usuarioAgenteRepo, alianzaDAO, sesionRepo, recursoRepo
  )
  lazy val autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminDriverRepository =
    AutorizacionUsuarioEmpresarialAdminDriverRepository(usuarioAdminRepo, sesionRepo, alianzaDAO, recursoRepo)
  lazy val preguntasRepo: PreguntasDriverRepository = PreguntasDriverRepository(preguntaDAO)
  lazy val preguntasValidacionRepository: PreguntasAutovalidacionDriverRepository =
    PreguntasAutovalidacionDriverRepository(preguntasRepo, configuracionRepo, alianzaDAO)
  lazy val ipRepo = IpDriverRepository(empresaAdminDAO, ipEmpresaDAO, ipUsuarioDAO, sesionRepo)
  lazy val rolRecursoComercialRepo = RolRecursoComercialDriverRepository(rolRecursoComercialDAO)
  lazy val autorizacionRecursoComercialRepository = AutorizacionRecursoComercialDriverRepository(rolRecursoComercialRepo)
  lazy val recursoComercialRepository = RecursoComercialDriverRepository(recursoComercialDAO, rolRecursoComercialDAO)
  lazy val rolComercialRepository = RolComercialDriverRepository(rolComercialDAO)
  lazy val actualizacionRepository = ActualizacionDriverRepository(actualizacionDAO)
  lazy val horarioEmpresaRepository = HorarioEmpresaDriverRepository(empresaRepo, horarioEmpresaDAO, diaFestivoDAO)
  lazy val pinRepository = PinDriverRepository(pinUsuarioDAO, pinAdminDAO, pinAgenteDAO, empresaRepo, ipUsuarioRepo, ipEmpresaRepo, usuarioRepo,
    usuarioAdminRepo, usuarioAgenteRepo, ultimaContrasenaRepo, reglaContrasenaRepo)

}

private[app] sealed trait StoragePGAlianzaDB extends BootedCore {
  implicit val config: DBConfig = new DBConfig with PGConfig
  implicit val configCore: DBConfig = new DBConfig with OracleConfig

  lazy val alianzaDAO = AlianzaDAO()(config)
  lazy val empresaDAO = EmpresaDAO()(config)
  lazy val usuarioDAO = UsuarioDAO()(config)
  lazy val ipUsuarioDAO = IpUsuarioDAO()(config)
  lazy val ipEmpresaDAO = IpEmpresaDAO()(config)
  lazy val actualizacionDAO = ActualizacionDAO()(ex, configCore)
  lazy val clienteDAO = ClienteDAO()(ex, configCore)
  lazy val configuracionDAO = ConfiguracionDAO()(config)
  lazy val reglaContrasenaDAO = ReglaContrasenaDAO()(config)
  lazy val usuarioAgenteDAO = UsuarioEmpresarialDAO()(config)
  lazy val respuestaUsuarioDAO = RespuestaUsuarioDAO()(config)
  lazy val usuarioAdminDAO = UsuarioEmpresarialAdminDAO()(config)
  lazy val respuestaUsuarioAdminDAO = RespuestaUsuarioAdminDAO()(config)
  lazy val preguntaDAO = PreguntasDAO()(config)
  lazy val empresaAdminDAO = EmpresaAdminDAO()(config)
  lazy val alianzaLdapDAO = AlianzaLdapDAO()
  lazy val usuarioComercialDAO = UsuarioComercialDAO()(config)
  lazy val usuarioComercialAdminDAO = UsuarioComercialAdminDAO()(config)
  lazy val rolRecursoComercialDAO = RolRecursoComercialDAO()(config)
  lazy val recursoComercialDAO = RecursoComercialDAO()(config)
  lazy val rolComercialDAO = RolComercialDAO()(config)
  lazy val horarioEmpresaDAO = HorarioEmpresaDAO()(config)
  lazy val diaFestivoDAO = DiaFestivoDAO()(config)
  lazy val ultimaContrasenaDAO = UltimaContrasenaDAO()(config)
  lazy val ultimaContrasenaAdminDAO = UltimaContrasenaAdminDAO()(config)
  lazy val ultimaContrasenaAgenteDAO = UltimaContrasenaAgenteDAO()(config)
  lazy val pinUsuarioDAO = PinUsuarioDAO()(config)
  lazy val pinAdminDAO = PinAdminDAO()(config)
  lazy val pinAgenteDAO = PinAgenteDAO()(config)
}

/**
 *
 */
object MainActors extends BootedCore with CoreActors
