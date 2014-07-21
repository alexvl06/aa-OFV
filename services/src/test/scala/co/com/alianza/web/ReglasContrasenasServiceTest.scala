package co.com.alianza.web

import co.com.alianza.infrastructure.messages.InboxMessage
import org.specs2.mutable.Specification
import spray.http.StatusCodes
import spray.testkit.Specs2RouteTest

import scala.concurrent.duration.DurationInt
import scala.util.Random

/**
 * Created by david on 18/06/14.
 */
class ReglasContrasenasServiceTest extends Specification with Specs2RouteTest {

  implicit val defaultTimeout = RouteTestTimeout( DurationInt( 240 ) seconds )

  val service = new ReglasContrasenasService

  "Reglas Contrasenas Service" should {

    "return status code 200 for GET requests to the path /reglasContrasenas" in {

      Get("/reglasContrasenas") ~> service.route ~> check {

        println("============== RESPONSE ============")
        println(response)
        println("===================================")
        status === StatusCodes.OK
      }
    }
  }

  // curl -X POST -H 'Content-Type: application/json' -d '{"reglasContrasenas": [{"CANTIDAD_REINTENTOS_INGRESO_CONTRASENA":"3"},{"DIAS_VALIDA":"3"}]}' http://localhost:4900/reglasContrasenas

}
