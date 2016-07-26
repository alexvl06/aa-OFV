package co.com.alianza.domain.aggregates.usuarios

import akka.actor.{Actor, ActorLogging}
import co.com.alianza.infrastructure.messages._
import co.com.alianza.persistence.util.DataBaseExecutionContext

import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.util.{Failure => sFailure, Success => sSuccess}
import scala.concurrent.ExecutionContext

/**
 * Created by manuel on 18/12/14.
 */
class UsuarioEmpresarialActor(implicit val system: ActorSystem) extends Actor with ActorLogging {
  import system.dispatcher

  def receive = {
    case message: ConsultaUsuarioEmpresarialMessage =>
      val currentSender = sender
      if (message.token.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioEmpresarialToken(message.token.get) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else if (message.nit.isDefined && message.usuario.isDefined) {
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialPorNitYUsuario(message.nit.get, message.usuario.get) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      } else
        currentSender ! None

    case message: ConsultaUsuarioEmpresarialAdminMessage =>
      val currentSender = sender
      if (message.token.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioEmpresarialAdminToken(message.token.get) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else if (message.nit.isDefined && message.usuario.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialAdminPorNitYUsuario(message.nit.get, message.usuario.get) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else
        currentSender ! None
  }

}
