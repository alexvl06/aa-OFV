package co.com.alianza.app

import akka.actor.{ ActorRef, ActorSystem, Props }
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
import co.com.alianza.persistence.entities.{ UsuarioAgenteInmobiliario, UsuarioEmpresarial }
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialDriverRepository, AutenticacionDriverRepository, AutenticacionEmpresaDriverRepository }
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteDriverCoreRepository
import portal.transaccional.autenticacion.service.drivers.configuracion.ConfiguracionDriverRepository
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.empresa.EmpresaDriverRepository
import portal.transaccional.autenticacion.service.drivers.ip.IpDriverRepository
import portal.transaccional.autenticacion.service.drivers.ipempresa.IpEmpresaDriverRepository
import portal.transaccional.autenticacion.service.drivers.ipusuario.IpUsuarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.ldap.LdapDriverRepository
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.pregunta.{ PreguntasAutovalidacionDriverRepository, PreguntasDriverRepository }
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoDriverRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaDriverRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.{ RespuestaUsuarioAdminDriverRepository, RespuestaUsuarioDriverRepository }
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{ RecursoComercialDriverRepository, RolComercialDriverRepository, RolRecursoComercialDriverRepository }
import portal.transaccional.autenticacion.service.drivers.sesion.SesionDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioEmpresarialAdminDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteEmpresarial.UsuarioEmpresarialDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario._
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.{ UsuarioComercialAdminDriverRepository, UsuarioComercialAdminRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioDriverRepository
import portal.transaccional.autenticacion.service.drivers.util.{ SesionAgenteUtilDriverRepository, SesionAgenteUtilRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.config.DBConfig
import portal.transaccional.fiduciaria.autenticacion.storage.config.pg.{ OracleConfig, PGConfig }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.core.ClienteDAO
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
  val usuariosActorSupervisor = system.actorOf(Props[UsuariosActorSupervisor], "UsuariosActorSupervisor")
  val usuariosActor = system.actorSelection(usuariosActorSupervisor.path)
  val confrontaActorSupervisor = system.actorOf(Props[ConfrontaActorSupervisor], "confrontaActorSupervisor")
  val confrontaActor = system.actorSelection(confrontaActorSupervisor.path)
  val consultaClienteActorSupervisor = system.actorOf(Props[ConsultaClienteActorSupervisor], "consultaClienteActorSupervisor")
  val consultaClienteActor = system.actorSelection(consultaClienteActorSupervisor.path)
  val contrasenasActorSupervisor = system.actorOf(Props[ContrasenasActorSupervisor], "contrasenasActorSupervisor")
  val contrasenasActor = system.actorSelection(contrasenasActorSupervisor.path)
  val contrasenasAgenteEmpresarialActorSupervisor = system.actorOf(
    Props[ContrasenasAgenteEmpresarialActorSupervisor],
    "contrasenasAgenteEmpresarialActorSupervisor"
  )
  val contrasenasAgenteEmpresarialActor = system.actorSelection(contrasenasAgenteEmpresarialActorSupervisor.path)

  val contrasenasClienteAdminActorSupervisor = system.actorOf(Props[ContrasenasClienteAdminActorSupervisor], "contrasenasClienteAdminActorSupervisor")
  val contrasenasClienteAdminActor = system.actorSelection(contrasenasClienteAdminActorSupervisor.path)

  val horarioEmpresaActorSupervisor = system.actorOf(Props[HorarioEmpresaActorSupervisor], "horarioEmpresaActorSupervisor")
  val horarioEmpresaActor = system.actorSelection(horarioEmpresaActorSupervisor.path)

  val pinActorSupervisor = system.actorOf(Props[PinActorSupervisor], "PinActorSupervisor")
  val pinActor = system.actorSelection(pinActorSupervisor.path)
  val pinUsuarioEmpresarialAdminActor = system.actorSelection(pinActorSupervisor.path + "/pinUsuarioEmpresarialAdminActor")
  val pinUsuarioAgenteEmpresarialActor = system.actorSelection(pinActorSupervisor.path + "/pinUsuarioAgenteEmpresarialActor")

  val sesionActorSupervisor = system.actorOf(Props[SesionActorSupervisor], "sesionActorSupervisor")
  val autorizacionActorSupervisor = system.actorOf(AutorizacionActorSupervisor.props(sesionActorSupervisor), "autorizacionActorSupervisor")

  val ipsUsuarioActorSupervisor = system.actorOf(IpsUsuarioActorSupervisor.props(sesionActorSupervisor), "ipsUsuarioActorSupervisor")
  val ipsUsuarioActor = system.actorSelection(ipsUsuarioActorSupervisor.path)

  val agenteEmpresarialActorSupervisor = system.actorOf(Props[AgenteEmpresarialActorSupervisor], "agenteEmpresarialActorSupervisor")
  val agenteEmpresarialActor = system.actorSelection(agenteEmpresarialActorSupervisor.path)

  val permisoTransaccionalActorSupervisor = system.actorOf(Props[PermisoTransaccionalActorSupervisor], "permisoTransaccionalActorSupervisor")
  val permisoTransaccionalActor = system.actorSelection(permisoTransaccionalActorSupervisor.path)

  val actualizacionActorSupervisor = system.actorOf(Props[ActualizacionActorSupervisor], "actualizacionActorSupervisor")
  val actualizacionActor = system.actorSelection(actualizacionActorSupervisor.path)

  val kafkaActorSupervisor = system.actorOf(Props[KafkaActorSupervisor], "kafkaActorSupervisor")
  val kafkaActor = system.actorSelection(kafkaActorSupervisor.path)
  val preguntasAutovalidacionSupervisor = system.actorOf(Props[PreguntasAutovalidacionSupervisor], "preguntasAutovalidacionSupervisor")
  val preguntasAutovalidacionActor = system.actorSelection(preguntasAutovalidacionSupervisor.path)
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
  lazy val reglaContrasenaRepo = ReglaContrasenaDriverRepository(reglaContrasenaDAO)
  lazy val usuarioAgenteRepo = UsuarioEmpresarialDriverRepository(usuarioAgenteDAO)
  lazy val usuarioAgenteInmobRepo = UsuarioAgenteInmobDriverRepository(usuarioAgenteInmob)
  lazy val agenteInmobRepo = UsuarioInmobiliarioDriverRepository(configuracionDAO, usuarioInmobDAO, pinAgenteInmobRepository)
  lazy val agenteInmobContrasenaRepo = ContrasenaAgenteInmobiliarioDriverRepository(agenteInmobRepo, ultimaContraseñaAgenteInmobDAO, reglaContrasenaRepo, pinAgenteInmobRepository)
  lazy val usuarioComercialRepo = UsuarioComercialDriverRepository(usuarioComercialDAO)
  lazy val usuarioComercialAdminRepo = UsuarioComercialAdminDriverRepository(usuarioComercialAdminDAO)
  lazy val respuestaUsuarioRepo = RespuestaUsuarioDriverRepository(respuestaUsuarioDAO, configuracionRepo)
  lazy val usuarioAdminRepo = UsuarioEmpresarialAdminDriverRepository(usuarioAdminDAO)
  lazy val respuestaUsuariAdminoRepo = RespuestaUsuarioAdminDriverRepository(respuestaUsuarioAdminDAO, configuracionRepo)
  lazy val autorizacionUsuarioRepo = AutorizacionUsuarioDriverRepository(usuarioRepo, recursoRepo, sesionRepo)
  lazy val autorizacionComercialRepo = AutorizacionUsuarioComercialDriverRepository(sesionRepo, usuarioComercialRepo)
  lazy val autorizacionComercialAdminRepo = AutorizacionUsuarioComercialAdminDriverRepository(sesionRepo, usuarioComercialAdminRepo)
  lazy val autenticacionRepo = AutenticacionDriverRepository(usuarioRepo, clienteRepo, configuracionRepo, reglaContrasenaRepo, ipUsuarioRepo,
    respuestaUsuarioRepo, sesionRepo)
  lazy val autenticacionEmpresaRepo = AutenticacionEmpresaDriverRepository(usuarioAgenteRepo, usuarioAdminRepo, clienteRepo, empresaRepo, reglaContrasenaRepo,
    configuracionRepo, ipEmpresaRepo, sesionRepo, respuestaUsuariAdminoRepo, usuarioAgenteInmobRepo)
  lazy val autenticacionComercialRepo = AutenticacionComercialDriverRepository(ldapRepo, usuarioComercialRepo,
    usuarioComercialAdminRepo: UsuarioComercialAdminRepository, configuracionRepo, sesionRepo)
  lazy val sesionUtilAgenteEmpresarial: SesionAgenteUtilRepository = SesionAgenteUtilDriverRepository[UsuarioEmpresarial](usuarioAgenteRepo, sesionRepo)
  lazy val sesionUtilAgenteInmobiliario: SesionAgenteUtilRepository = SesionAgenteUtilDriverRepository[UsuarioAgenteInmobiliario](usuarioAgenteInmobRepo, sesionRepo)
  lazy val autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialDriverRepository = AutorizacionUsuarioEmpresarialDriverRepository(
    usuarioAgenteRepo, alianzaDAO, sesionRepo, recursoRepo, sesionUtilAgenteEmpresarial
  )
  lazy val autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminDriverRepository =
    AutorizacionUsuarioEmpresarialAdminDriverRepository(usuarioAdminRepo, sesionRepo, alianzaDAO, recursoRepo)
  lazy val preguntasRepo: PreguntasDriverRepository = PreguntasDriverRepository(preguntaDAO)
  lazy val preguntasValidacionRepository: PreguntasAutovalidacionDriverRepository = PreguntasAutovalidacionDriverRepository(preguntasRepo, configuracionRepo, alianzaDAO)
  lazy val ipRepo = IpDriverRepository(usuarioRepo, usuarioAgenteRepo, empresaAdminDAO, ipEmpresaDAO, usuarioAdminRepo, clienteRepo, ipUsuarioDAO)
  lazy val rolRecursoComercialRepo = RolRecursoComercialDriverRepository(rolRecursoComercialDAO)
  lazy val autorizacionRecursoComercialRepository = AutorizacionRecursoComercialDriverRepository(rolRecursoComercialRepo)
  lazy val recursoComercialRepository = RecursoComercialDriverRepository(recursoComercialDAO, rolRecursoComercialDAO)
  lazy val rolComercialRepository = RolComercialDriverRepository(rolComercialDAO)
  lazy val permisoAgenteInmob = PermisoAgenteInmobiliarioDriverRepository(alianzaDAO, usuarioInmobDAO, permisoInmobDAO, recursoInmobiliarioDAO)
  lazy val pinAgenteInmobRepository = UsuarioInmobiliarioPinDriverRepository(pinInmobDAO)
  lazy val  autorizacionAgenteInmob: AutorizacionRepository = AutorizacionDriverRepository(sesionRepo, alianzaDAO, recursoRepo)
}

private[app] sealed trait StoragePGAlianzaDB extends BootedCore {
  implicit val config: DBConfig = new DBConfig with PGConfig
  implicit val configCore: DBConfig = new DBConfig with OracleConfig

  lazy val alianzaDAO = AlianzaDAO()(config)
  lazy val empresaDAO = EmpresaDAO()(config)
  lazy val usuarioDAO = UsuarioDAO()(config)
  lazy val ipUsuarioDAO = IpUsuarioDAO()(config)
  lazy val ipEmpresaDAO = IpEmpresaDAO()(config)
  lazy val clienteDAO = ClienteDAO()(ex, configCore)
  lazy val configuracionDAO = ConfiguracionDAO()(config)
  lazy val reglaContrasenaDAO = ReglaContrasenaDAO()(config)
  lazy val usuarioAgenteDAO: UsuarioEmpresarialDAO = UsuarioEmpresarialDAO()(config)
  lazy val respuestaUsuarioDAO = RespuestaUsuarioDAO()(config)
  lazy val usuarioAdminDAO = UsuarioEmpresarialAdminDAO()(config)
  lazy val usuarioAgenteInmob: UsuarioAgenteInmobDAO = UsuarioAgenteInmobDAO()(config)
  lazy val respuestaUsuarioAdminDAO = RespuestaUsuarioAdminDAO()(config)
  lazy val preguntaDAO = PreguntasDAO()(config)
  lazy val empresaAdminDAO = EmpresaAdminDAO()(config)
  lazy val alianzaLdapDAO = AlianzaLdapDAO()
  lazy val usuarioComercialDAO = UsuarioComercialDAO()(config)
  lazy val usuarioComercialAdminDAO = UsuarioComercialAdminDAO()(config)
  lazy val rolRecursoComercialDAO = RolRecursoComercialDAO()(config)
  lazy val recursoComercialDAO = RecursoComercialDAO()(config)
  lazy val rolComercialDAO = RolComercialDAO()(config)
  lazy val usuarioInmobDAO = UsuarioAgenteInmobDAO()(config)
  lazy val permisoInmobDAO = PermisoInmobiliarioDAO()(config)
  lazy val ultimaContraseñaAgenteInmobDAO = UltimaContraseñaAgenteInmobiliarioDAO()(config)
  lazy val recursoInmobiliarioDAO = RecursoInmobiliarioDAO()(config)
  lazy val pinInmobDAO = PinAgenteInmobiliarioDAO()(config)
}

object MainActors extends BootedCore with CoreActors
