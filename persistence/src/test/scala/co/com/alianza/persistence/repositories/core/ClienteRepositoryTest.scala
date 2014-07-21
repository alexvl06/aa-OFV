package co.com.alianza.persistence.repositories.core

import org.scalatest.FunSuite
import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import co.com.alianza.persistence.messages.ConsultaClienteRequest

/**
 *
 * @author seven4n
 */
class ClienteRepositoryTest extends FunSuite {

  implicit val conf: Config = ConfigFactory.load
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  test("ClienteRepository Test") {

    val repo = new ClienteRepository

    Try {
      Await.result(repo.consultaCliente(ConsultaClienteRequest(1, "800122772")), 60 seconds)
      //Await.result(repo.consultaCliente(ConsultaClienteRequest("any", "19388904")), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST cliente OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST cliente OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }
}

