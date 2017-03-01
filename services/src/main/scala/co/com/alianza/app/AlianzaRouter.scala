package co.com.alianza.app

import akka.actor.{ ActorLogging, ActorSelection, ActorSystem }
import co.com.alianza.app.handler.CustomRejectionHandler
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.persistence.entities.UsuarioEmpresarial
import co.com.alianza.web._
import co.com.alianza.web.empresa.UsuarioEmpresaService
import portal.transaccional.autenticacion.service.drivers.actualizacion.ActualizacionRepository
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionComercialRepository, AutenticacionEmpresaRepository, AutenticacionRepository }
import portal.transaccional.autenticacion.service.drivers.autorizacion._
import portal.transaccional.autenticacion.service.drivers.contrasena.{ ContrasenaUsuarioRepository, ContrasenaAdminRepository, ContrasenaAgenteRepository }
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.horarioEmpresa.HorarioEmpresaRepository
import portal.transaccional.autenticacion.service.drivers.ip.IpRepository
import portal.transaccional.autenticacion.service.drivers.permisoAgenteInmobiliario.PermisoAgenteInmobiliarioRepository
import portal.transaccional.autenticacion.service.drivers.pin.PinRepository
import portal.transaccional.autenticacion.service.drivers.pregunta.PreguntasAutovalidacionRepository
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.respuesta.RespuestaUsuarioRepository
import portal.transaccional.autenticacion.service.drivers.rolRecursoComercial.{ RecursoComercialRepository, RolComercialRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.{ AutorizacionRepository, UsuarioInmobiliarioPinRepository, UsuarioInmobiliarioRepository }
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.autenticacion.service.drivers.util.SesionAgenteUtilRepository
import portal.transaccional.autenticacion.service.web.actualizacion.ActualizacionService
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.{ AgenteImobiliarioPinService, AgenteInmobiliarioService }
import portal.transaccional.autenticacion.service.web.autenticacion.AutenticacionService
import portal.transaccional.autenticacion.service.web.autorizacion.{ AutorizacionRecursoComercialService, AutorizacionService }
import portal.transaccional.autenticacion.service.web.comercial.ComercialService
import portal.transaccional.autenticacion.service.web.contrasena.{ AdministrarContrasenaService, AdministrarContrasenaEmpresaService }
import portal.transaccional.autenticacion.service.web.enumeracion.EnumeracionService
import portal.transaccional.autenticacion.service.web.horarioEmpresa.HorarioEmpresaService
import portal.transaccional.autenticacion.service.web.ip.{ IpService, PinService }
import portal.transaccional.autenticacion.service.web.preguntasAutovalidacion.PreguntasAutovalidacionService
import portal.transaccional.autenticacion.service.web.recursoComercial.RecursoGraficoComercialService
import portal.transaccional.autenticacion.service.web.reglaContrasena.ReglaContrasenaService
import portal.transaccional.autenticacion.service.web.sesion.SesionService
import spray.routing._
import spray.util.LoggingContext

case class AlianzaRouter(autenticacionRepo: AutenticacionRepository, autenticacionEmpresaRepositorio: AutenticacionEmpresaRepository,
  autenticacionComercialRepositorio: AutenticacionComercialRepository, usuarioRepositorio: UsuarioRepository,
  usuarioAgenteRepositorio: UsuarioEmpresarialRepository[UsuarioEmpresarial], usuarioAdminRepositorio: UsuarioAdminRepository,
  autorizacionUsuarioRepo: AutorizacionUsuarioRepository, kafkaActor: ActorSelection, usuariosActor: ActorSelection, confrontaActor: ActorSelection,
  actualizacionRepo: ActualizacionRepository, permisoTransaccionalActor: ActorSelection, agenteEmpresarialActor: ActorSelection, pinRepo: PinRepository,
  contrasenasAgenteRepo: ContrasenaAgenteRepository, contrasenasAdminRepo: ContrasenaAdminRepository, contrasenasRepo: ContrasenaUsuarioRepository,
  autorizacionAgenteRepo: AutorizacionUsuarioEmpresarialRepository, autorizacionAdminRepo: AutorizacionUsuarioEmpresarialAdminRepository,
  preguntasValidacionRepository: PreguntasAutovalidacionRepository, respuestaUsuarioRepository: RespuestaUsuarioRepository,
  respuestaUsuarioAdminRepository: RespuestaUsuarioRepository, ipRepo: IpRepository, autorizacionComercialRepo: AutorizacionUsuarioComercialRepository,
  autorizacionComercialAdminRepo: AutorizacionUsuarioComercialAdminRepository, autorizacionRecursoComercialRepository: AutorizacionRecursoComercialRepository,
  recursoComercialRepository: RecursoComercialRepository, rolComercialRepository: RolComercialRepository,
  usuarioComercialAdminRepo: UsuarioComercialAdminRepository, reglaRepo: ReglaContrasenaRepository, horarioEmpresaRepository: HorarioEmpresaRepository,
  usInmobiliarioRepo: UsuarioInmobiliarioRepository, permisoAgenteInmob: PermisoAgenteInmobiliarioRepository,
  sesionUtilAgenteInmobiliario: SesionAgenteUtilRepository, agenteInmobContrasenaRepo: ContrasenaAgenteInmobiliarioRepository,
  pinAgenteInmobRepository: UsuarioInmobiliarioPinRepository, autorizacionInmobRepo: AutorizacionRepository)(implicit val system: ActorSystem)
    extends HttpServiceActor with RouteConcatenation with CrossHeaders with ServiceAuthorization
    with ActorLogging {

  import system.dispatcher

  val routes =
    AutorizacionService(kafkaActor, usuarioRepositorio, usuarioAgenteRepositorio, usuarioAdminRepositorio, autorizacionUsuarioRepo,
      autorizacionAgenteRepo, autorizacionAdminRepo, autorizacionComercialRepo, autorizacionComercialAdminRepo, sesionUtilAgenteInmobiliario,
      autorizacionInmobRepo).route ~ AutenticacionService(autenticacionRepo, autenticacionEmpresaRepositorio, autenticacionComercialRepositorio,
      kafkaActor).route ~
      //TODO: refactorizar
      new ConfrontaService(confrontaActor).route ~
      new EnumeracionService().route ~
      //TODO: refactorizar
      UsuarioService(kafkaActor, usuariosActor).route ~
      ReglaContrasenaService(reglaRepo).route ~
      //TODO: refactorizar
      PinService(kafkaActor, pinRepo).route ~
      AdministrarContrasenaService(kafkaActor, contrasenasRepo, contrasenasAgenteRepo, contrasenasAdminRepo, agenteInmobContrasenaRepo).route ~
      AgenteImobiliarioPinService(pinAgenteInmobRepository, agenteInmobContrasenaRepo).route ~
      authenticate(authenticateUser) {
        user =>
          IpService(user, kafkaActor, ipRepo).route ~
            SesionService().route ~
            AgenteInmobiliarioService(user, usInmobiliarioRepo, permisoAgenteInmob, agenteInmobContrasenaRepo).route ~
            RecursoGraficoComercialService(user, kafkaActor, recursoComercialRepository, rolComercialRepository).route ~
            AutorizacionRecursoComercialService(user, kafkaActor, autorizacionRecursoComercialRepository).route ~
            ComercialService(user, kafkaActor, usuarioComercialAdminRepo).route ~
            ActualizacionService(user, kafkaActor, actualizacionRepo).route ~
            HorarioEmpresaService(user, kafkaActor, horarioEmpresaRepository).route ~
            //TODO: refactorizar
            AdministrarContrasenaService(kafkaActor, contrasenasRepo, contrasenasAgenteRepo, contrasenasAdminRepo, agenteInmobContrasenaRepo).routeSeguro(user) ~
            // TODO Cambiar al authenticate de cliente empresarial
            AdministrarContrasenaEmpresaService(user, kafkaActor, contrasenasAgenteRepo, contrasenasAdminRepo).route ~
            //TODO: refactorizar
            UsuarioEmpresaService(kafkaActor, agenteEmpresarialActor).secureUserRouteEmpresa(user) ~
            //TODO: refactorizar
            PermisosTransaccionalesService(kafkaActor, permisoTransaccionalActor).route(user) ~
            PreguntasAutovalidacionService(user, kafkaActor, preguntasValidacionRepository, respuestaUsuarioRepository, respuestaUsuarioAdminRepository).route
      }

  def receive = runRoute(respondWithHeaders(listCrossHeaders) { routes })(
    ExceptionHandler.default,
    CustomRejectionHandler.extended orElse RejectionHandler.Default,
    context,
    RoutingSettings.default,
    LoggingContext.fromActorRefFactory
  )

}