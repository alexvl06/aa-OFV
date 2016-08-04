package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.infrastructure.messages.{ InvalidarSesion, ValidarSesion }
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.Recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.{ DataAccessTranslator, UsuarioRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioDAOs

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioDriverRepository(usuarioRepo: UsuarioRepository, recursoRepo: RecursoRepository, sessionActor: ActorRef)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizarUrl(token: String, url: String): Future[ValidacionAutorizacion] = {
    val encriptedToken = AesUtil.encriptarToken(token, "AutorizacionUsuarioDriverRepository.autorizarUrl")
    for {
      validar <- validarToken(token)
      validarSesion <- Future(actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(encriptedToken)))
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
      recursos <- recursoRepo.obtenerRecursos(usuario.id.get)
      validarRecurso <- resolveMessageRecursos(usuario, recursos, url)
    } yield validarRecurso
  }

  def invalidarToken(token: String): Future[Int] = {
    for {
      x <- usuarioRepo.invalidarToken(token)
      _ <- sessionActor ? InvalidarSesion(token)
    } yield x
  }

  private def validarUsario(usuarioOption: Option[Usuario]): Future[Usuario] = {
    usuarioOption match {
      case Some(usuario: Usuario) => Future.successful(usuario)
      case _ => Future.failed(NoAutorizado("usuario no existe"))
    }
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(usuario: Usuario, recursos: Seq[RecursoPerfil], url: String): Future[ValidacionAutorizacion] = Future {
    val usuarioDTO: UsuarioDTO = DataAccessTranslator.entityToDto(usuario)
    val recursosFiltro = recursoRepo.filtrarRecursos(recursos, url)
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursos.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(usuarioDTO)
            Autorizado(usuarioJson)
        }
    }
  }

  private def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }

}

case class ForbiddenMessage(usuario: UsuarioDTO, filtro: Option[String])