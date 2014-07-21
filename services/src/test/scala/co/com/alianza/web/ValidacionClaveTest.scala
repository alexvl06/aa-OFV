package co.com.alianza.web

import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest.FunSuite
import co.com.alianza.util.clave._
import scala.concurrent.{Await, Future}
import scalaz.Validation
import co.com.alianza.exceptions.PersistenceException
import scalaz.{Success => zSuccess, Failure => zFailure}

//

/**
 *
 * @author smontanez
 */
class ValidacionClaveTest  extends FunSuite {

  implicit val conf: Config = ConfigFactory.load
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  test("Test ValidacionClave") {

    val resultFuture = Await.result(validaClave("1#aDsdaaZ1?"), 60 seconds)
    resultFuture match {
      case zSuccess(value) =>
        println(" error validacion => "  + value.toString())
      case zFailure(error) =>
        println(" error persistence => "  + error)
    }


    //validaClave("123456").Aw

  }



  private def validaClave(clave:String): Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = {
    ValidarClave.aplicarReglas(clave,ValidarClave.reglasGenerales: _*)
  }


}
