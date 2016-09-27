package portal.transaccional.autenticacion.service.web.agenteInmobiliario

import akka.actor.ActorSystem
import org.specs2.mutable.Specification
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

class AgenteInmobiliarioServiceSpec extends Specification with Specs2RouteTest with HttpService {

  def actorRefFactory: ActorSystem = system

}
