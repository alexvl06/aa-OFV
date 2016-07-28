package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.messages.ValidarSesion
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.Recurso.RecursoRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAOs, UsuarioDAOs }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioDriverRepository (usuarioDAO: UsuarioDAOs, rescursoRepo: RecursoRepository, sessionActor: ActorRef)
  (implicit val ex: ExecutionContext) extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizarUrl(token: String, url: String): Future[Boolean] = {
    for {
      validar <- validarToken(token)
      usuarioOption <- usuarioDAO.getByToken(token)
      usuario <- validarUsario(usuarioOption)
      validarSesion <- actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(token))
      recursos <- rescursoRepo.obtenerRecursos(usuario.id.get)
      validarRecurso <- resolveMessageRecursos(recursos.filter(rescursoRepo.filtrarRecursos(_, url)))
    } yield validarRecurso
  }

  def validarUsario(usuarioOption: Option[Usuario]): Future[Usuario] = {
    usuarioOption match {
      case Some(usuario: Usuario) => Future.successful(usuario)
      case _ => Future.failed(ValidacionException("500", "usuario no existe"))
      //TODO: validar si ese es el codigo
    }
  }

  def validarToken(token: String): Future[Boolean] = {
    val util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
    val tokenDesencriptado = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    Token.autorizarToken(tokenDesencriptado) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("500", "Token erróneo"))
      //TODO: validar si ese es el codigo
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(recursos: Seq[RecursoPerfil]): Future[Boolean] = {
    recursos.nonEmpty match {
      case false => Future.failed(ValidacionException("403.1", "No tiene permiso"))
      case true =>
        recursos.head.filtro match {
          case Some(filtro) => Future.failed(ValidacionException("403.2", "No tiene permiso"))
          case None => Future.successful(true)
        }
    }
  }

  private def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }

}
