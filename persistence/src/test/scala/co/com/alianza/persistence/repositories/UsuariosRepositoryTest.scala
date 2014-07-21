package co.com.alianza.persistence.repositories

import org.scalatest.FunSuite
import com.typesafe.config.{ConfigFactory, Config}
import scala.util.{Failure, Success, Try}
import scala.concurrent.{Future, Await}
import co.com.alianza.persistence.entities.Usuario
import java.sql.Timestamp
import scalaz.{OptionT, Validation}
import co.com.alianza.exceptions.PersistenceException

import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

/**
 *
 * @author seven4n
 */
class UsuariosRepositoryTest extends FunSuite {

  implicit val conf: Config = ConfigFactory.load
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global
/*
  test("UsuariosRepository obtenerUsuarios") {

    val repo = new UsuariosRepository

    Try {
      Await.result(repo.obtenerUsuarios(), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST usuarios OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST usuarios OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }
*/

  test("UsuariosRepository obtenerUsuarioNumeroIdentificacion") {

    val repo = new UsuariosRepository

    Try {

     Await.result(repo.obtenerUsuarioNumeroIdentificacion("1020722933"), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST obtenerUsuarioNumeroIdentificacion OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST obtenerUsuarioNumeroIdentificacion OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }
/*
  test("UsuariosRepository guardar") {

    val repo = new UsuariosRepository

    Try {
      //id: Int, correo: String, nombre: String, fechaCaducidad: Timestamp, identificacion: String, tipoIdentificacion: Int, estado: Int, contrasena: String)
      val usuario = Usuario(None,"aa@aaa.com", "Pedro", new Timestamp(System.currentTimeMillis()), "1045289", 1,1,"564897895646")
      Await.result(repo.guardar(usuario), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST usuarios OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST usuarios OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }*/
}

