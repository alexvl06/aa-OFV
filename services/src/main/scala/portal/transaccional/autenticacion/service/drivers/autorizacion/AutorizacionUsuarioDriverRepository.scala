package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor.SesionUsuarioValidada
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.messages.{ CrearSesionUsuario, ValidarSesion }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioDAOs

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.concurrent.duration._

/**
 * Created by hernando on 27/07/16.
 */
case class AutorizacionUsuarioDriverRepository(usuarioDAO: UsuarioDAOs, sessionActor: ActorRef)(implicit val ex: ExecutionContext) {

  implicit val timeout = Timeout(5.seconds)

  def autorizarUrl(token: String, url: String): Future[SesionUsuarioValidada] = {
    for{
      validar <- validarToken(token)
      validarSesion <- actorResponse[SesionActorSupervisor.SesionUsuarioValidada](sessionActor, ValidarSesion(token))
      //usuarioOption <- usuarioDAO.getByToken(token)
    }yield validarSesion
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

  def actorResponse[T: ClassTag](actor: ActorRef, msg: ValidarSesion): Future[T] = {
    (actor ? msg).mapTo[T]
  }
}

//
//
//  /**
//   * Se valida si el recurso solicitado esta asociado al usuario
//   * @return
//   */
//  private def validarRecurso(usuarioOpt: Option[Usuario], url: String) = {
//    usuarioOpt match {
//      case Some(usuario) =>
//        val recursosFuturo = rDataAccessAdapter.obtenerRecursos(usuario.id.get)
//        recursosFuturo.map(_.map(x => resolveMessageRecursos(usuario, x.filter(filtrarRecursos(_, url)))))
//      case _ =>
//        Future.successful(Validation.success(ResponseMessage(Unauthorized, TokenInvalido().msg)))
//    }
//
//  }
//
//  /**
//   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
//   * @param recursos Listado de recursos
//   * @return
//   */
//  private def resolveMessageRecursos(usuario: Usuario, recursos: List[RecursoUsuario]) = {
//    recursos.isEmpty match {
//      case true => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenMessage(usuario, None, "403.1")))
//      case false =>
//        val recurso = recursos.head
//        recurso.filtro match {
//          case Some(filtro) => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenMessage(usuario, recurso.filtro, "403.2")))
//          case None => ResponseMessage(OK, JsonUtil.toJson(usuario))
//        }
//    }
//  }
//
//  /**
//   * Filtra el listado de recursos que concuerden con la url
//   *
//   * @param recurso recursos asociados al usuario
//   * @param url la url a validar
//   * @return
//   */
//  private def filtrarRecursos(recurso: RecursoUsuario, url: String): Boolean =
//    filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)
//
//  protected def filtrarRecursos(urlRecurso: String, acceso: Boolean, url: String) = {
//    //TODO: quitar esos "ifseses"
//    if (urlRecurso.equals(url)) acceso
//    else if (urlRecurso.endsWith("/*")) {
//      val urlC = urlRecurso.substring(0, urlRecurso.lastIndexOf("*"))
//      if (urlC.equals(url + "/")) acceso
//      else {
//        if (url.length >= urlC.length) {
//          //TODO: Whhhattt ??? if (url.endsWith("/")) "" else ""
//          val ends = if (url.endsWith("/")) "" else ""
//          val urlSuffix = url.substring(0, urlC.length) + ends
//          if (urlSuffix.equals(urlC)) acceso
//          else false
//        } else false
//      }
//
//    } else false
//  }
//
//}
//
//case class ForbiddenMessage(usuario: Usuario, filtro: Option[String], code: String)
//
//
//}
