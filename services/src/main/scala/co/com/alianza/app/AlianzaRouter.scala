package co.com.alianza.app

import akka.actor.{ ActorLogging, ActorSelection, ActorSystem }
import co.com.alianza.app.handler.CustomRejectionHandler
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.web.empresa.{ AdministrarContrasenaEmpresaService, UsuarioEmpresaService }
import spray.routing._
import spray.util.LoggingContext
import co.com.alianza.web._
import co.com.alianza.webvalidarPinClienteAdmin.PinService
import portal.transaccional.autenticacion.service.drivers.autenticacion.AutenticacionRepository

case class AlianzaRouter(autenticacionRepo: AutenticacionRepository, kafkaActor: ActorSelection, preguntasAutovalidacionActor: ActorSelection,
    usuariosActor: ActorSelection, confrontaActor: ActorSelection, autenticacionActor: ActorSelection, autenticacionUsuarioEmpresaActor: ActorSelection,
    actualizacionActor: ActorSelection, permisoTransaccionalActor: ActorSelection, agenteEmpresarialActor: ActorSelection,
    pinActor: ActorSelection, pinUsuarioEmpresarialAdminActor: ActorSelection, pinUsuarioAgenteEmpresarialActor: ActorSelection, ipsUsuarioActor: ActorSelection,
    horarioEmpresaActor: ActorSelection, autorizacionActor: ActorSelection,
    autorizacionUsuarioEmpresarialActor: ActorSelection, contrasenasAgenteEmpresarialActor: ActorSelection, contrasenasClienteAdminActor: ActorSelection,
    contrasenasActor: ActorSelection)(implicit val system: ActorSystem) extends HttpServiceActor with RouteConcatenation with CrossHeaders
  with ServiceAuthorization with ActorLogging {

  import system.dispatcher

  val routes =
    AutorizacionService(kafkaActor, autorizacionActor, autorizacionUsuarioEmpresarialActor).route ~
      portal.transaccional.autenticacion.service.web.autenticacion.AutenticacionService(autenticacionRepo, kafkaActor).route ~
      AutenticacionService(kafkaActor, autenticacionActor, autenticacionUsuarioEmpresaActor).route ~
      new ConfrontaService(confrontaActor).route ~
      new EnumeracionService().route ~
      UsuarioService(kafkaActor, usuariosActor).route ~
      new ReglasContrasenasService(contrasenasActor).route ~
      PinService(kafkaActor, pinActor, pinUsuarioAgenteEmpresarialActor, pinUsuarioEmpresarialAdminActor).route ~
      new AdministrarContrasenaService(kafkaActor, contrasenasActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor).insecureRoute ~
      authenticate(authenticateUser) {
        user =>
          IpsUsuariosService(kafkaActor, ipsUsuarioActor).route(user) ~
            IpsUsuariosService(kafkaActor, ipsUsuarioActor).route(user) ~
            ActualizacionService(actualizacionActor, kafkaActor).route(user) ~
            HorarioEmpresaService(kafkaActor, horarioEmpresaActor).route(user) ~
            new AdministrarContrasenaService(kafkaActor, contrasenasActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor).secureRoute(user) ~
            AutenticacionService(kafkaActor, autenticacionActor, autenticacionUsuarioEmpresaActor).routeAutenticado(user) ~
            //TO-DO Cambiar al authenticate de cliente empresarial o agente
            new AdministrarContrasenaEmpresaService(kafkaActor, contrasenasAgenteEmpresarialActor, contrasenasClienteAdminActor).secureRouteEmpresa(user) ~
            UsuarioEmpresaService(kafkaActor, agenteEmpresarialActor).secureUserRouteEmpresa(user) ~
            PermisosTransaccionalesService(kafkaActor, permisoTransaccionalActor).route(user) ~
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