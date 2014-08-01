package co.com.alianza.domain.aggregates.usuarios

import akka.actor.{ActorRef, Actor, ActorLogging}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import scala.util.{Success, Failure}
import co.com.alianza.app.{MainActors, AlianzaActors}
import co.com.alianza.infrastructure.messages.{ResponseMessage, UsuarioMessage}
import spray.http.StatusCodes._
import scala.concurrent.Future
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.util.transformers.ValidationT
import scalaz.std.AllInstances._
import co.com.alianza.util.clave.Crypto
import com.typesafe.config.{ConfigFactory, Config}
import co.com.alianza.exceptions.PersistenceException

/**
 *
 */
class UsuariosActor extends Actor with ActorLogging with AlianzaActors {
  import scala.concurrent.ExecutionContext
  implicit val ex: ExecutionContext = MainActors.dataAccesEx
  implicit private val config: Config = ConfigFactory.load
  import ValdiacionesUsuario._

  def receive = {

    case message:UsuarioMessage  =>

      val currentSender = sender()

      val crearUsuarioFuture = (for{
        captchaVal <-  ValidationT(validaCaptcha(message))
        cliente <- ValidationT(validaSolicitud(message))
        idUsuario <- ValidationT(guardarUsuario(message))
      }yield{
        idUsuario
      }).run

      resolveCrearUsuarioFuture(crearUsuarioFuture,currentSender,message)


  }

  private def validaSolicitud(message:UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {

    val consultaNumDocFuture = validacionConsultaNumDoc(message)
    val consultaCorreoFuture: Future[Validation[ErrorValidacion, Unit.type]] = validacionConsultaCorreo(message)
    val consultaClienteFuture: Future[Validation[ErrorValidacion, Cliente]] = validacionConsultaCliente(message)
    val validacionClave: Future[Validation[ErrorValidacion, Unit.type]] = validacionReglasClave(message)

    val f1:Future[Validation[ErrorPersistence, String]] = ???

    val f2:Future[Validation[ErrorPersistence, String]] = ???

    val x: Future[Validation[ErrorPersistence, String]] = (for{

      s1 <-  ValidationT(f1)
      s2 <-  ValidationT(f2)

    }yield{

      s"""$s1-$s2"""

    }).run


    val futureList: Future[List[Validation[PersistenceException, String]]] = ???



    futureList onSuccess{
      case listaValidation: List[Validation[PersistenceException, String]] =>


        for( aaa: Validation[PersistenceException, String] <-  listaValidation){

        }

        for{
          aaa:String <-  ValidationT(listaValidation)
        }yield{
          println(" aaa " + aaa)
        }

    }



    (for{
      resultValidacionClave <- ValidationT(validacionClave)
      resultConsultaNumDoc <- ValidationT(consultaNumDocFuture)
      resultConsultaCorreo <- ValidationT(consultaCorreoFuture)
      cliente <- ValidationT(consultaClienteFuture)
    }yield {
      cliente
    }).run
  }

  private def guardarUsuario(message:UsuarioMessage): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUsuario.crearUsuario(message.toEntityUsuario( Crypto.hashSha256(message.contrasena))).map(_.leftMap( pe => ErrorPersistence(pe.message,pe)))
  }

  private def resolveCrearUsuarioFuture(crearUsuarioFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef,message:UsuarioMessage) = {
    crearUsuarioFuture onComplete {
      case Failure(failure)  =>    currentSender ! failure
      case Success(value)    =>
        value match {
          case zSuccess(response: Int) =>
            currentSender !  ResponseMessage(Created, response.toString)
            if(message.activarIP && message.clientIp.isDefined){
              DataAccessAdapterUsuario.relacionarIp(response,message.clientIp.get)
            }
          case zFailure(error)  =>
            error match {
              case errorPersistence:ErrorPersistence  => currentSender !  errorPersistence.exception
              case errorVal:ErrorValidacion =>
                currentSender !  ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

}