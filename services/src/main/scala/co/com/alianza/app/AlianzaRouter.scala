package co.com.alianza.app

import akka.actor.ActorSystem
import co.com.alianza.infrastructure.security.ServiceAuthorization
import co.com.alianza.web._
import com.typesafe.config.Config
import spray.routing.{RouteConcatenation, HttpServiceActor}
import spray.http.StatusCodes
import StatusCodes._

class AlianzaRouter extends HttpServiceActor with RouteConcatenation with CrossHeaders  with ServiceAuthorization {

  implicit val contextAuthorization = MainActors.ex
  val conf: Config= MainActors.conf
  val system: ActorSystem = MainActors.system

  val routes =
    new AutorizacionService().route ~
    new AutenticacionService( ).route ~
    new ConfrontaService().route ~
    new EnumeracionService().route ~
    new ClienteCoreService().route ~
    new UsuarioService().route ~
    new ReglasContrasenasService().route ~
    new PinService().route ~
    authenticate(authenticateUser) {
      user =>
        new IpsUsuariosService().route(user) ~
        new AdministrarContrasenaService().route(user)
    }

  def receive = runRoute(
    respondWithHeaders(listCrossHeaders){
      routes ~ options {
        complete(OK)
      }
    })

}