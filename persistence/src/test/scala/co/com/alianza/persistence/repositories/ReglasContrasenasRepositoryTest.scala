//package co.com.alianza.persistence.repositories
//
//import co.com.alianza.persistence.entities.ReglasContrasenas
//import com.typesafe.config.{ ConfigFactory, Config }
//import org.scalatest.FunSuite
//
//import scala.concurrent.Await
//import scala.util.{ Failure, Success, Try }
//import co.com.alianza.util.ConfigApp
//
///**
// * Created by david on 12/06/14.
// */
//class ReglasContrasenasRepositoryTest extends FunSuite {
//
//  implicit val conf: Config = ConfigApp.conf
//  import scala.concurrent.duration._
//  import scala.concurrent.ExecutionContext.Implicits.global
//
//  test("ReglasContrasenasRepository obtenerReglas") {
//
//    val repo = new ReglasContrasenasRepository()
//
//    Try {
//      Await.result(repo.obtenerReglas(), 60 seconds)
//    } match {
//      case Success(response) =>
//        println("============= TEST ReglasContrasenasRepository OK - Success =============")
//        println(response)
//        println("================================================================")
//      case Failure(exception) =>
//        println("============= TEST ReglasContrasenasRepository OK - Failure =============")
//        println(exception)
//        println("================================================================")
//    }
//  }
//
//  test("ReglasContrasenasRepository obtenerRegla Especifica") {
//
//    val repo = new ReglasContrasenasRepository()
//
//    Try {
//      Await.result(repo.obtenerRegla("CARACTERES_RESTRINGIDOS"), 60 seconds)
//    } match {
//      case Success(response) =>
//        println("============= TEST ReglasContrasenasRepository OK - Success =============")
//        println(response)
//        println("================================================================")
//      case Failure(exception) =>
//        println("============= TEST ReglasContrasenasRepository OK - Failure =============")
//        println(exception)
//        println("================================================================")
//    }
//  }
//
//  test("ReglasContrasenasRepository actualizar regla Especifica") {
//
//    val repo = new ReglasContrasenasRepository()
//
//    Try {
//      Await.result(repo.actualizar(new ReglasContrasenas("CARACTERES_RESTRINGIDOS", "?")), 60 seconds)
//    } match {
//      case Success(response) =>
//        println("============= TEST ReglasContrasenasRepository OK - Success =============")
//        println(response)
//        println("================================================================")
//      case Failure(exception) =>
//        println("============= TEST ReglasContrasenasRepository OK - Failure =============")
//        println(exception)
//        println("================================================================")
//    }
//  }
//
//}
