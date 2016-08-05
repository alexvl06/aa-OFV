package co.com.alianza.persistence.repositories

import co.com.alianza.persistence.entities.IpsUsuario
import co.com.alianza.util.ConfigApp
import com.typesafe.config.Config
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.util.{Failure, Random, Success, Try}

/**
 * Created by david on 12/06/14.
 */
class IpsUsuarioRepositoryTest extends FunSuite {

  implicit val conf: Config = ConfigApp.conf
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  test("IpsUsuarioRepository obtenerIpsUsuario") {

    val repo = new IpsUsuarioRepository()

    Try {
      Await.result(repo.obtenerIpsUsuario(), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST IpsUsuarioRepository OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST IpsUsuarioRepository OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }

  test("IpsUsuarioRepository obtenerIpsUsuarioEspecifico") {

    val repo = new IpsUsuarioRepository()

    Try {
      Await.result(repo.obtenerIpsUsuario(1), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST IpsUsuarioRepository OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST IpsUsuarioRepository OK - Failure =============")
        println(exception)
        println("================================================================")
    }

    Try {
      Await.result(repo.obtenerIpsUsuario(2), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST IpsUsuarioRepository OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST IpsUsuarioRepository OK - Failure =============")
        println(exception)
        println("================================================================")
    }

  }

  test("IpsUsuarioRepository guardar") {

    val repo = new IpsUsuarioRepository()

    Try {
      Await.result(repo.guardar(IpsUsuario(2, Random.nextInt(500) + "")), 60 seconds)
    } match {
      case Success(response) =>
        println("============= TEST IpsUsuarioRepository OK - Success =============")
        println(response)
        println("================================================================")
      case Failure(exception) =>
        println("============= TEST IpsUsuarioRepository OK - Failure =============")
        println(exception)
        println("================================================================")
    }
  }

}
