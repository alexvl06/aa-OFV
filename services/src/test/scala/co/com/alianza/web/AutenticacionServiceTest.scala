/*package co.com.alianza.web

import scala.concurrent.duration.DurationInt

import spray.http._
import spray.http.MediaTypes._
import spray.testkit.Specs2RouteTest

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import co.com.alianza.infrastructure.messages.{ AutenticacionMessagesJsonSupport, AutenticarMessage }
import spray.http.HttpHeaders.`Remote-Address`

/**
 *
 * @author seven4n
 */
class AutenticacionServiceTest extends Specification with Specs2RouteTest {

  import AutenticacionMessagesJsonSupport._

  implicit val defaultTimeout = RouteTestTimeout(DurationInt(240) seconds)

  val headersRemoteAddress = List(`Remote-Address`("127.0.0.1"))

  val service = new AutenticacionService

  "Autenticacion Service" should {

    "return status code 200 for GET requests to the path /autenticar" in {

      Post("/autenticar", AutenticarMessage(1, "890114778", "2a8610aefdd0028c6bf074dd18721c0ef8bc43241cc7a653d7aedf2036bdf6b3")).withHeaders(headersRemoteAddress) ~> service.route ~> check {

        println("============== RESPONSE ============")
        println(response)
        println("===================================")

        status === StatusCodes.OK
      }
    }

    "return status code 409 for GET requests to the path /autenticar user not exists" in {

      Post("/autenticar", AutenticarMessage(1, "1234567", "1234567")) ~> service.route ~> check {

        println("============== RESPONSE ============")
        println(response)
        println("===================================")

        status === StatusCodes.Conflict
      }
    }

  }

}*/