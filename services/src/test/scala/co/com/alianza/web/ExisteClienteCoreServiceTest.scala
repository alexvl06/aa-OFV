package co.com.alianza.web

import scala.concurrent.duration.DurationInt

import spray.http._
import spray.testkit.Specs2RouteTest

import org.specs2.mutable.Specification

/**
 *
 * @author seven4n
 */
class ExisteClienteCoreServiceTest extends Specification with Specs2RouteTest {

  implicit val defaultTimeout = RouteTestTimeout( DurationInt( 240 ) seconds )

  val service = new ClienteCoreService

  " ExisteCliente Service" should {

    "return status code 200 for GET requests to the path /existeClienteCoreAlianza" in {

      Get( "/existeClienteCoreAlianza/1/80059968" )  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response )
        println( "===================================" )
        status === StatusCodes.OK
      }
    }

    "return status code 404 for GET requests to the path /existeClienteCoreAlianza" in {

      Get( "/existeClienteCoreAlianza/1/8001227711" )  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response )
        println( "===================================" )
        status === StatusCodes.NotFound
      }
    }
  }

}