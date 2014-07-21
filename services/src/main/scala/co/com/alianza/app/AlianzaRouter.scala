package co.com.alianza.app

import co.com.alianza.web.{ClienteCoreService, EnumeracionService, ConfrontaService, AutenticacionService, UsuarioService, ReglasContrasenasService}
import spray.routing.{RouteConcatenation, HttpServiceActor}
import spray.http.StatusCodes
import StatusCodes._

class AlianzaRouter extends HttpServiceActor with RouteConcatenation with CrossHeaders {

  val routes= new AutenticacionService( ).route ~
    new ConfrontaService().route ~
    new EnumeracionService().route ~
    new ClienteCoreService().route ~
    new UsuarioService().route ~
    new ReglasContrasenasService().route

  def receive = runRoute(
    respondWithHeaders(listCrossHeaders){
      routes ~ options {
        complete(OK)
      }
    })

}