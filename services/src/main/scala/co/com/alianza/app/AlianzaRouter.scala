package co.com.alianza.app

import akka.actor.{ActorSystem, ActorLogging}
import co.com.alianza.app.handler.CustomRejectionHandler
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.web._
import co.com.alianza.web.empresa.{UsuarioEmpresaService, AdministrarContrasenaEmpresaService}
import com.typesafe.config.Config
import spray.routing._
import spray.http.StatusCodes
import spray.util.LoggingContext
import StatusCodes._
import co.com.alianza.webvalidarPinClienteAdmin.PinService

class AlianzaRouter extends HttpServiceActor with RouteConcatenation with CrossHeaders  with ServiceAuthorization with ActorLogging {


  implicit val conf: Config = MainActors.conf
  implicit val system: ActorSystem = MainActors.system
  implicit val contextAuthorization = MainActors.ex

  val routes =
    new AutorizacionService().route ~
    new AutenticacionService( ).route ~
    new ConfrontaService().route ~
    new EnumeracionService().route ~
    new UsuarioService().route ~
    new ReglasContrasenasService().route ~
    new PinService().route ~
    new AdministrarContrasenaService().insecureRoute ~
  new ClienteCoreService().route ~
    authenticate(authenticateUser) {
      user =>
        new IpsUsuariosService().route(user) ~
        new HorarioEmpresaService().route(user) ~
        new AdministrarContrasenaService().secureRoute(user) ~
        new AutenticacionService().routeAutenticado( user ) ~
        //TO-DO Cambiar al authenticate de cliente empresarial o agente
        new AdministrarContrasenaEmpresaService().secureRouteEmpresa( user ) ~
        new UsuarioEmpresaService().secureUserRouteEmpresa( user ) ~
        new PermisosTransaccionalesService().route( user )
    }

  def receive = runRoute(
    respondWithHeaders(listCrossHeaders){
      routes
    })(ExceptionHandler.default,
      CustomRejectionHandler.extended orElse RejectionHandler.Default,
      context,
      RoutingSettings.default,
      LoggingContext.fromActorRefFactory)

}