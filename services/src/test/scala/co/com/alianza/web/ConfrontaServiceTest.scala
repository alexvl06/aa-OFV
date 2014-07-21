package co.com.alianza.web

import scala.concurrent.duration.DurationInt

import spray.http._
import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification

/**
 *
 * @author seven4n
 */
class ConfrontaServiceTest extends Specification with Specs2RouteTest {

  implicit val defaultTimeout = RouteTestTimeout( DurationInt( 240 ) seconds )

  val service = new ConfrontaService

  "Confronta Service" should {

    "return status code 200 for GET requests to the path /confronta/obtenerCuestionario" in {

      Get( "/confronta/obtenerCuestionario")  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response.entity.asString)
        println( "===================================" )

        status === StatusCodes.OK
      }
    }
  }

}