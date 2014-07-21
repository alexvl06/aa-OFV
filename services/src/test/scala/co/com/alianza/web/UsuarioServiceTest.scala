package co.com.alianza.web

import scala.concurrent.duration.DurationInt

import spray.http._
import spray.testkit.Specs2RouteTest

import org.specs2.mutable.Specification
import co.com.alianza.infrastructure.messages.{UsuariosMessagesJsonSupport, UsuarioMessage}
import scala.util.Random
import spray.http.HttpHeaders.`Remote-Address`
import spray.http.HttpMethods._
import co.com.alianza.infrastructure.messages.UsuarioMessage
import scala.concurrent.duration.DurationInt

/**
 *
 * @author seven4n
 */
class UsuarioServiceTest extends Specification with Specs2RouteTest {

  implicit val defaultTimeout = RouteTestTimeout( DurationInt( 240 ) seconds )
  import UsuariosMessagesJsonSupport._

  val headersRemoteAddress = List(`Remote-Address`("127.0.0.1"))

  val service = new UsuarioService

  "Usuario Service" should {

    "return status code 409 for PUT requests to the  /usuario" in {

      Put( "/autoregistro/usuario", UsuarioMessage("aaa", Random.nextInt(500)+"121212", 1, "1#aDsdaaZ1&",false)).withHeaders(headersRemoteAddress)  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response )
        println( "===================================" )
        status === StatusCodes.Conflict
      }
    }
   "return status code 409 for PUT requests to the path /usuario " in {

      Put( "/autoregistro/usuario", UsuarioMessage("aaa", "1020722933", 1, "1#aDsdaaZ1&",false)).withHeaders(headersRemoteAddress)  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response )
        println( "===================================" )
        status === StatusCodes.Conflict
      }
    }

    "return status code 409 for PUT requests to the path /usuario " in {

      Put( "/autoregistro/usuario", UsuarioMessage("test@test.com", "800122772", 1, "1#aDsdaaZ1&",false)).withHeaders(headersRemoteAddress)  ~> service.route ~> check {

        println( "============== RESPONSE ============" )
        println( response )
        println( "===================================" )
        status === StatusCodes.Conflict
      }
    }
  }

}