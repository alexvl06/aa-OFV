package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.infrastructure.messages.ValidarSesion
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.Recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.DataAccessTranslator
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioDAOs

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioDriverRepository(usuarioDAO: UsuarioDAOs, recursoRepo: RecursoRepository, sessionActor: ActorRef)
  (implicit val ex: ExecutionContext) extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  val aesUtil = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)

  def autorizarUrl(token: String, url: String): Future[ValidacionAutorizacion] = {
    val encriptedToken = encriptarToken(token)
    for {
      validar <- validarToken(encriptedToken)
      usuarioOption <- usuarioDAO.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
      validarSesion <- actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(encriptedToken))
      recursos <- recursoRepo.obtenerRecursos(usuario.id.get)
      validarRecurso <- resolveMessageRecursos(usuario, recursos, url)
    } yield validarRecurso
  }

  def encriptarToken(token: String): String = {
    aesUtil.encrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
  }

  def desencriptarToken(encriptedToken: String): String = {
    aesUtil.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, encriptedToken)
  }

  def validarUsario(usuarioOption: Option[Usuario]): Future[Usuario] = {
    usuarioOption match {
      case Some(usuario: Usuario) => Future.successful(usuario)
      case _ => Future.failed(NoAutorizado("usuario no existe"))
    }
  }

  def validarToken(encriptedToken: String): Future[Boolean] = {
    val token = desencriptarToken(encriptedToken)
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