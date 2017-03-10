package portal.transaccional.autenticacion.service.util.ws

import spray.routing.{ Directives, Route, RouteConcatenation }

/**
 * Define funciones y valores comunes para los servicios web.
 */
trait CommonService extends Directives with RouteConcatenation {

  def route: Route

}

