package co.com.alianza.app

import akka.actor.{ ActorLogging, ActorRef, ActorSelection, ActorSystem }
import co.com.alianza.app.handler.CustomRejectionHandler
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.web.empresa.{ AdministrarContrasenaEmpresaService, UsuarioEmpresaService }
import com.typesafe.config.Config
import spray.routing._
import spray.util.LoggingContext
import co.com.alianza.web._
import co.com.alianza.webvalidarPinClienteAdmin.PinService
import portal.transaccional.autenticacion.service.drivers.autenticacion.{ AutenticacionDriverRepository, AutenticacionRepository }

case class AlianzaRouter(autenticacionRepo: AutenticacionRepository, kafkaActor: ActorSelection, preguntasValidacionActor : ActorSelection)(implicit val system: ActorSystem) extends HttpServiceActor
  with RouteConcatenation with CrossHeaders with ServiceAuthorization with ActorLogging {

  import system.dispatcher

  val routes =
      AutorizacionService(kafkaActor).route ~
      portal.transaccional.autenticacion.service.web.autenticacion.AutenticacionService(autenticacionRepo, kafkaActor).route ~
      AutenticacionService(kafkaActor).route ~
      new ConfrontaService().route ~
      new EnumeracionService().route ~
      UsuarioService(kafkaActor).route ~
      new ReglasContrasenasService().route ~
      PinService(kafkaActor).route ~
      new AdministrarContrasenaService().insecureRoute ~
      authenticate(authenticateUser) {
        user =>
            IpsUsuariosService(kafkaActor).route(user) ~
            new ActualizacionService().route(user) ~
            HorarioEmpresaService(kafkaActor).route(user) ~
            new AdministrarContrasenaService().secureRoute(user) ~
            AutenticacionService(kafkaActor).routeAutenticado(user) ~
            //TO-DO Cambiar al authenticate de cliente empresarial o agente
            new AdministrarContrasenaEmpresaService().secureRouteEmpresa(user) ~
            new UsuarioEmpresaService().secureUserRouteEmpresa(user) ~
            PermisosTransaccionalesService(kafkaActor).route(user) ~
            PreguntasAutovalidacionService(kafkaActor, preguntasValidacionActor).route(user)
      }

  def receive = runRoute(
    respondWithHeaders(listCrossHeaders) {
      routes
    }
  )(
    ExceptionHandler.default,
    CustomRejectionHandler.extended orElse RejectionHandler.Default,
    context,
    RoutingSettings.default,
    LoggingContext.fromActorRefFactory
  )

}