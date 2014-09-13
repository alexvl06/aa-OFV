package co.com.alianza.persistence.repositories

import org.scalatest.FunSuite
import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import scalaz.OptionT

import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.util.ConfigApp

/**
 *
 * @author seven4n
 */
class RecursosUsuarioRepositoryTest extends FunSuite {

  implicit val conf: Config = ConfigApp.conf
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global


  test("UsuariosRepository obtenerUsuarioNumeroIdentificacion") {

    val repo = new RecursosPerfilRepository

    Try {

       Await.result(repo.obtenerRecursos(84), 60 seconds)

    } match {
      case Success(response) =>
        println("============= TEST obtenerRecursos OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST obtenerRecursos OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }

}

