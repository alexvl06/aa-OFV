package co.com.alianza.persistence.repositories

import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}
import scala.util.Random
import co.com.alianza.persistence.entities.IpsUsuario

/**
 * Created by david on 12/06/14.
 */
class IpsUsuarioRepositoryTest extends FunSuite{

  implicit val conf: Config = ConfigFactory.load
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

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
      Await.result(repo.guardar(IpsUsuario(2, Random.nextInt(500)+ "")), 60 seconds)
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
