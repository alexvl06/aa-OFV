package portal.transaccional.autenticacion.service.web

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FlatSpec, Matchers }
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.routing.Route
import spray.testkit.ScalatestRouteTest

trait RouteTest extends FlatSpec
    with Matchers with ScalatestRouteTest with MockFactory with CommonRESTFul with DomainJsonFormatters {

  // no need to implement route for testing
  override def route: Route = ???
}
