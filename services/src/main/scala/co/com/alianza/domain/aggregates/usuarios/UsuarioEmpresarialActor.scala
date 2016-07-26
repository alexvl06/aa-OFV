package co.com.alianza.domain.aggregates.usuarios

import akka.actor.{ Actor, ActorLogging, ActorSystem }
import co.com.alianza.infrastructure.messages._

import scala.util.{ Failure => sFailure, Success => sSuccess }
import scalaz.{ Failure => zFailure, Success => zSuccess }

/**
 * Created by manuel on 18/12/14.
 */
class UsuarioEmpresarialActor extends Actor with ActorLogging {
  import system.dispatcher

  def receive = {
    case message: ConsultaUsuarioEmpresarialMessage =>
      val currentSender = sender
      if (message.token.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioEmpresarialToken(
          message.token.get
        ) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else if (message.nit.isDefined && message.usuario.isDefined) {
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtieneUsuarioEmpresarialPorNitYUsuario(
          message.nit.get, message.usuario.get
        ) onComplete {
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
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter.obtenerUsuarioEmpresarialAdminToken(
          message.token.get
        ) onComplete {
          case sFailure(failure) =>
            currentSender ! failure
          case sSuccess(value) => value match {
            case zSuccess(response) => currentSender ! response
            case zFailure(error) => currentSender ! error
          }
        }
      else if (message.nit.isDefined && message.usuario.isDefined)
        co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
          .obtieneUsuarioEmpresarialAdminPorNitYUsuario(message.nit.get, message.usuario.get) onComplete {
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
