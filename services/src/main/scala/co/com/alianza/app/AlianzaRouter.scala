package co.com.alianza.app

import akka.actor.{ ActorLogging, ActorRef, ActorSelection, ActorSystem }
import co.com.alianza.app.handler.CustomRejectionHandler
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import co.com.alianza.web.empresa.{ AdministrarContrasenaEmpresaService, UsuarioEmpresaService }
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import spray.routing._
import spray.util.LoggingContext
import co.com.alianza.web._
import co.com.alianza.webvalidarPinClienteAdmin.PinService
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialRepository, AutenticacionEmpresaRepository, AutenticacionRepository }
import portal.transaccional.autenticacion.service.drivers.ip.IpRepository
import portal.transaccional.autenticacion.service.drivers.pregunta.PreguntasAutovalidacionRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioEmpresarialAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.autenticacion.service.web.autorizacion.{ AutorizacionRecursoComercialService, AutorizacionService }
import portal.transaccional.autenticacion.service.web.autenticacion.AutenticacionService
import portal.transaccional.autenticacion.service.web.sesion.SesionService
import co.com.alianza.web.PreguntasAutovalidacionService
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{ RecursoComercialRepository, RolComercialRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.util.SesionAgenteUtilRepository
import portal.transaccional.autenticacion.service.web.ip.IpService
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.AgenteInmobiliarioService
import portal.transaccional.autenticacion.service.web.recursoComercial.RecursoGraficoComercialService

case class AlianzaRouter(
    autenticacionRepo: AutenticacionRepository, autenticacionEmpresaRepositorio: AutenticacionEmpresaRepository,
    autenticacionComercialRepositorio: AutenticacionComercialRepository, usuarioRepositorio: UsuarioRepository,
    usuarioAgenteRepositorio: UsuarioEmpresarialRepository[UsuarioEmpresarial], usuarioAdminRepositorio: UsuarioEmpresarialAdminRepository,
    autorizacionUsuarioRepo: AutorizacionUsuarioRepository, kafkaActor: ActorSelection, preguntasAutovalidacionActor: ActorSelection,
    usuariosActor: ActorSelection, confrontaActor: ActorSelection, actualizacionActor: ActorSelection, permisoTransaccionalActor: ActorSelection,
    agenteEmpresarialActor: ActorSelection, pinActor: ActorSelection, pinUsuarioEmpresarialAdminActor: ActorSelection,
    pinUsuarioAgenteEmpresarialActor: ActorSelection, ipsUsuarioActor: ActorSelection, horarioEmpresaActor: ActorSelection,
    contrasenasAgenteEmpresarialActor: ActorSelection, contrasenasClienteAdminActor: ActorSelection, contrasenasActor: ActorSelection,
    autorizacionActorSupervisor: ActorRef, autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository,
    autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository, preguntasValidacionRepository: PreguntasAutovalidacionRepository,
    respuestaUsuarioRepository: RespuestaUsuarioRepository, respuestaUsuarioAdminRepository: RespuestaUsuarioRepository, ipRepo: IpRepository,
    autorizacionComercialRepo: AutorizacionUsuarioComercialRepository, autorizacionComercialAdminRepo: AutorizacionUsuarioComercialAdminRepository,
    autorizacionRecursoComercialRepository: AutorizacionRecursoComercialRepository, recursoComercialRepository: RecursoComercialRepository,
    rolComercialRepository: RolComercialRepository, usInmobiliarioRepo: UsuarioInmobiliarioRepository, permisoAgenteInmob: PermisoAgenteInmobiliarioRepository,
    sesionUtilAgenteEmpresarial: SesionAgenteUtilRepository, sesionUtilAgenteInmobiliario: SesionAgenteUtilRepository,
    agenteInmobContrasenaRepo: ContrasenaAgenteInmobiliarioRepository
)(implicit val system: ActorSystem) extends HttpServiceActor with RouteConcatenation with CrossHeaders with ServiceAuthorization with ActorLogging {

  import system.dispatcher

  val routes =
    //    AgenteInmobiliarioService(permisoAgenteInmob).route ~
    AutorizacionService(usuarioRepositorio, usuarioAgenteRepositorio, usuarioAdminRepositorio, autorizacionUsuarioRepo, kafkaActor, autorizacionAgenteRepo,
      autorizacionAdminRepo, autorizacionComercialRepo, autorizacionComercialAdminRepo, sesionUtilAgenteEmpresarial, sesionUtilAgenteInmobiliario).route ~
      AutenticacionService(autenticacionRepo, autenticacionEmpresaRepositorio, autenticacionComercialRepositorio, kafkaActor).route ~
      RecursoGraficoComercialService(recursoComercialRepository, rolComercialRepository).route ~
      AutorizacionRecursoComercialService(autorizacionRecursoComercialRepository).route ~
      ConfrontaService(confrontaActor).route ~
      new EnumeracionService().route ~
      UsuarioService(kafkaActor, usuariosActor).route ~
      ReglasContrasenasService(contrasenasActor).route ~
      PinService(kafkaActor, pinActor, pinUsuarioEmpresarialAdminActor, pinUsuarioAgenteEmpresarialActor).route ~
      AdministrarContrasenaService(kafkaActor, contrasenasActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor, agenteInmobContrasenaRepo).insecureRoute ~
      authenticate(authenticateUser) {
        user =>
          IpsUsuariosService(kafkaActor, ipsUsuarioActor).route(user) ~
            IpService(user, ipRepo).route ~
            SesionService().route ~
            AgenteInmobiliarioService(user, usInmobiliarioRepo, permisoAgenteInmob, agenteInmobContrasenaRepo).route ~
            ActualizacionService(actualizacionActor, kafkaActor).route(user) ~
            HorarioEmpresaService(kafkaActor, horarioEmpresaActor).route(user) ~
            new AdministrarContrasenaService(kafkaActor, contrasenasActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor, agenteInmobContrasenaRepo).secureRoute(user) ~
            // TODO Cambiar al authenticate de cliente empresarial
            new AdministrarContrasenaEmpresaService(kafkaActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor).secureRouteEmpresa(user) ~
            UsuarioEmpresaService(kafkaActor, agenteEmpresarialActor).secureUserRouteEmpresa(user) ~
            PermisosTransaccionalesService(kafkaActor, permisoTransaccionalActor).route(user) ~
            // TODO: Servicio Nuevo de PreguntasAutovalidacionService by: Jonathan
            /*portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.PreguntasAutovalidacionService(
              user, preguntasValidacionRepository, respuestaUsuarioRepository, respuestaUsuarioAdminRepository).route*/
            PreguntasAutovalidacionService(kafkaActor, preguntasAutovalidacionActor).route(user)
      }

  def receive = runRoute(respondWithHeaders(listCrossHeaders) { routes })(
    ExceptionHandler.default,
    CustomRejectionHandler.extended orElse RejectionHandler.Default,
    context,
    RoutingSettings.default,
    LoggingContext.fromActorRefFactory
  )

}