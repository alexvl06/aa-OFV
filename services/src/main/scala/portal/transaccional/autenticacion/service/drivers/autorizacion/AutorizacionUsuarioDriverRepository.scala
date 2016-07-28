package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.infrastructure.messages.ValidarSesion
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.Recurso.RecursoRepository
import portal.transaccional.autenticacion.service.dtt.usuario.DataAccessTranslator
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioDAOs

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioDriverRepository(usuarioDAO: UsuarioDAOs, recursoRepo: RecursoRepository, sessionActor: ActorRef)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  val aesUtil = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)

  def autorizarUrl(token: String, url: String): Future[UsuarioDTO] = {
    val encriptedToken = encriptarToken(token)
    for {
      validar <- validarToken(encriptedToken)
      usuarioOption <- usuarioDAO.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
      validarSesion <- actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(token))
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
      case _ => Future.failed(ValidacionException("500", "usuario no existe"))
      //TODO: validar si ese es el codigo
    }
  }

  def validarToken(encriptedToken: String): Future[Boolean] = {
    val token = desencriptarToken(encriptedToken)
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("500", "Token erróneo"))
      //TODO: validar si ese es el codigo
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(usuario: Usuario, recursos: Seq[RecursoPerfil], url: String): Future[UsuarioDTO] = {
    val recursosFiltro = recursoRepo.filtrarRecursos(recursos, url)
    recursosFiltro.nonEmpty match {
      case false => Future.failed(ValidacionException("403.1", "No tiene permiso"))
      case true =>
        recursos.head.filtro match {
          case Some(filtro) => Future.failed(ValidacionException("403.2", "No tiene permiso"))
          case None =>
            val usuarioDTO = DataAccessTranslator.entityToDto(usuario)
            Future.successful(usuarioDTO)
        }
    }
  }

  private def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }

}
