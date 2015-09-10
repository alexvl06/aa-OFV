package co.com.alianza.web

import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.FunSuite
import co.com.alianza.util.clave._
import scala.concurrent.{Await, Future}
import scalaz.Validation
import co.com.alianza.exceptions.PersistenceException
import scalaz.{Success => zSuccess, Failure => zFailure}
import co.com.alianza.util.ConfigApp

//

/**
 *
 * @author smontanez
 */
class ValidacionClaveTest  extends FunSuite {

  implicit val conf: Config = ConfigApp.conf
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

 /* test("Test ValidacionClave") {

    val resultFuture = Await.result(validaClave("1#aDsdaaZ1?"), 60 seconds)
    resultFuture match {
      case zSuccess(value) =>
        println(" error validacion => "  + value.toString())
      case zFailure(error) =>
        println(" error persistence => "  + error)
    }



  }



  private def validaClave(clave:String): Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = {
    ValidarClave.aplicarReglas(clave,Some(123456), ValidarClave.reglasGenerales)
  }*/


}
