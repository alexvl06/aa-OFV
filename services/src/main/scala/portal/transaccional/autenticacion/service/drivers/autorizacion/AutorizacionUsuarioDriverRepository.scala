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
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAOs, UsuarioDAOs }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

/**
 * Created by hernando on 27/07/16.
 */
//TODO: Crear el repo para los recursos y no usar el alianza dao
case class AutorizacionUsuarioDriverRepository(usuarioDAO: UsuarioDAOs, alianzaDAO: AlianzaDAOs, sessionActor: ActorRef)(implicit val ex: ExecutionContext)
    extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  val aesUtil = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)

  def autorizarUrl(token: String, url: String): Future[Boolean] = {
    val encriptedToken = encriptarToken(token)
    for {
      validar <- validarToken(encriptedToken)
      usuarioOption <- usuarioDAO.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
      validarSesion <- actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(encriptedToken))
      recursos <- alianzaDAO.getResources(usuario.id.get)
      validarRecurso <- resolveMessageRecursos(recursos.filter(filtrarRecursos(_, url)))
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
    println("--------------------")
    println("--------------------")
    println("validarToken")
    println("--------------------")
    println(encriptedToken)
    val token = desencriptarToken(encriptedToken)
    Token.autorizarToken(token) match {
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

  /**
   * Filtra el listado de recursos que concuerden con la url
   *
   * @param recurso recursos asociados al usuario
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursos(recurso: RecursoPerfil, url: String): Boolean = filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

  private def filtrarRecursos(urlRecurso: String, acceso: Boolean, url: String) = {
    val urlC = urlRecurso.substring(0, urlRecurso.lastIndexOf("*"))
    if (urlRecurso.equals(url) || (urlRecurso.endsWith("/*") && urlC.equals(url + "/"))) {
      acceso
    } else if (urlRecurso.endsWith("/*") && url.length >= urlC.length) {
      url.substring(0, urlC.length).equals(urlC) && acceso
    } else { false }
  }

  private def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }

}
