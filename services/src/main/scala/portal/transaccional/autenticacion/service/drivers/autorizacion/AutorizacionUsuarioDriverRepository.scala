package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.{RecursoUsuario}
import co.com.alianza.infrastructure.messages.ValidarSesion
import co.com.alianza.persistence.entities.{Usuario => UsuarioDTO}
import co.com.alianza.persistence.entities.Usuario
import co.com.alianza.util.token.{Token, AesUtil}
import enumerations.CryptoAesParameters
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{AlianzaDAOs, UsuarioDAOs}

import scala.concurrent.{Future, ExecutionContext}
import scala.reflect.ClassTag
import scala.concurrent.duration._

/**
 * Created by hernando on 27/07/16.
 */
case class AutorizacionUsuarioDriverRepository(usuarioDAO: UsuarioDAOs, alianzaDAO: AlianzaDAOs, sessionActor: ActorRef)(implicit val ex: ExecutionContext) {

  implicit val timeout = Timeout(5.seconds)

  def autorizarUrl(token: String, url: String): Future[Boolean] = {
    for{
      validar <- validarToken(token)
      usuarioOption <- usuarioDAO.getByToken(token)
      usuario <- validarUsario(usuarioOption)
      validarSesion <- Future{true}//Llamar al actoractorResponse[SesionActorSupervisor.SesionUsuarioCreada](sessionActor, CrearSesionUsuario(token, inactividad.valor.toInt))

      recursosFuturo = alianzaDAO.getResources(usuario.id.get)

      validarrecurso <- resolveMessageRecursos(usuario, x.filter(filtrarRecursos(_, url))

    }yield validarSesion
  }

  def validarUsario(usuarioOption: Option[Usuario]): Future[Usuario] = {
    usuarioOption match {
      case Some(usuario: Usuario) => Future.successful(usuario)
      case _ => Future.failed(ValidacionException("500" , "usuario no existe"))
      //TODO: validar si ese es el codigo
    }
  }

  def validarToken(token: String): Future[Boolean] = {
    val util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
    val tokenDesencriptado = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, token)
    Token.autorizarToken(tokenDesencriptado) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("500" , "Token erróneo"))
      //TODO: validar si ese es el codigo
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(recursos: List[RecursoUsuario]): Future[Boolean] = {
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
  private def filtrarRecursos(recurso: RecursoUsuario, url: String): Boolean =
    filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

  private def filtrarRecursos(urlRecurso: String, acceso: Boolean, url: String) = {
    //TODO: quitar esos "ifseses"
    if (urlRecurso.equals(url)) acceso
    else if (urlRecurso.endsWith("/*")) {
      val urlC = urlRecurso.substring(0, urlRecurso.lastIndexOf("*"))
      if (urlC.equals(url + "/")) acceso
      else {
        if (url.length >= urlC.length) {
          val urlSuffix = url.substring(0, urlC.length)
          urlSuffix.equals(urlC) && acceso
        } else false
      }
    } else false
  }

  def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }

}

case class ForbiddenMessage(usuario: Usuario, filtro: Option[String], code: String)