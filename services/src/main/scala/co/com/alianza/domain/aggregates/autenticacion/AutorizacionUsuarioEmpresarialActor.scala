package co.com.alianza.domain.aggregates.autenticacion

import akka.pattern.ask

import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial, UsuarioEmpresarialAdmin, RecursoPerfilAgente, RecursoPerfilClienteAdmin}
import co.com.alianza.infrastructure.anticorruption.recursosAgenteEmpresarial.{DataAccessAdapter => raDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.recursosClienteAdmin.{DataAccessAdapter => rcaDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usDataAdapter}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors

import scala.concurrent.Future
import scalaz.std.AllInstances._
import scalaz.Validation

import spray.http.StatusCodes._

/**
 * Created by manuel on 16/12/14.
 */
class AutorizacionUsuarioEmpresarialActor extends AutorizacionActor {

  override def receive = {

    case message: AutorizarUsuarioEmpresarialMessage =>
      val currentSender = sender()
      val future = (for {
        usuarioOption <- ValidationT(validarToken(message))
//        validacionIp <- ValidationT(validarIpEmpresa(message.ip))
        result <- ValidationT(validarRecursoAgente(usuarioOption, message.url))
//        result <- ValidationT(validarUsuario(usuarioOption))
      } yield {
        result
      }).run
      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)

    case message: AutorizarUsuarioEmpresarialAdminMessage =>
      val currentSender = sender()
      val future = (for {
        usuarioOption <- ValidationT(validarTokenAdmin(message.token))
        result <- ValidationT(validarRecursoClienteAdmin(usuarioOption, message.url))
//        result <- ValidationT(validarUsuario(usuarioOption))
      } yield {
        result
      }).run
      resolveFutureValidation(future, (x: ResponseMessage) => x, currentSender)

  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param message El token para realizar validación
   */
  private def validarToken(message: AutorizarUsuarioEmpresarialMessage): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = {
    Token.autorizarToken(message.token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialToken(message.token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarial]]] = x.map { userOpt =>
            guardaTokenCache(userOpt, message)
          }
          co.com.alianza.util.transformers.Validation.sequence(y)
        }
      case false =>
        Future.successful(Validation.success(None))
    }
  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param token El token para realizar validación
   */
  private def validarTokenAdmin(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = {
    Token.autorizarToken(token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialAdminToken(token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarialAdmin]]] = x.map { userOpt =>
            guardaTokenAdminCache(userOpt, token)
          }
          co.com.alianza.util.transformers.Validation.sequence(y)
        }
      case false =>
        Future.successful(Validation.success(None))
    }
  }

  private def validarIpEmpresa(ip: String) = {
    ask(MainActors.sesionActorSupervisor, ValidarIpEmpresa(ip)).mapTo[Boolean]
  }

  /**
   *
   * Si usuarioOption tiene un valor se guarda en cache y retorna el usuario sin el campo contraseña
   * @param usuarioOption Option con el usuario
   * @param message El token
   * @return
   */
  private def guardaTokenCache(usuarioOption: Option[UsuarioEmpresarial], message: AutorizarUsuarioEmpresarialMessage): Future[Option[UsuarioEmpresarial]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(message.token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map(usuario => usuario.copy(contrasena = None))
      case false => None
    }
  }

  /**
   *
   * Si usuarioOption tiene un valor se guarda en cache y retorna el usuario sin el campo contraseña
   * @param usuarioOption Option con el usuario
   * @param token El token
   * @return
   */
  private def guardaTokenAdminCache(usuarioOption: Option[UsuarioEmpresarialAdmin], token: String): Future[Option[UsuarioEmpresarialAdmin]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map(usuario => usuario.copy(contrasena = None))
      case false => None
    }
  }

  /**
   * Valida si el usuario existe y responde
   * @param usuarioOp Option
   * @return ResponseMessage con el statusCode correspondiente
   */
  private def validarUsuario(usuarioOp: Option[Any]) = {
    usuarioOp match {
      case None => Future.successful(Validation.success(ResponseMessage(Unauthorized, "Error Validando Token")))
      case Some(us) => Future.successful(Validation.success(ResponseMessage(OK, JsonUtil.toJson(us))))
    }
  }

  private def validarRecursoAgente(agente: Option[UsuarioEmpresarial], url: Option[String]) =
    agente match {
      case Some(usuario) =>
        val recursosFuturo = raDataAccessAdapter obtenerRecursos usuario.id
        recursosFuturo.map(_.map(x => resolveMessageRecursosAgente(usuario, x.filter(filtrarRecursosPerfilAgente(_, url.getOrElse(""))))))
      case _ =>
        Future.successful(Validation.success(ResponseMessage(Unauthorized, "Error Validando Token")))
    }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursosAgente(usuario: UsuarioEmpresarial, recursos: List[RecursoPerfilAgente]) = {
    recursos.isEmpty match {
      case true => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, None, "403.1")))
      case false =>
        val recurso = recursos.head

        recurso.filtro match {
          case Some(filtro) => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, recurso.filtro, "403.2")))
          case None => ResponseMessage(OK, JsonUtil.toJson(usuario))

        }

    }
  }

  private def validarRecursoClienteAdmin(clienteAdmin: Option[UsuarioEmpresarialAdmin], url: Option[String]) =
    clienteAdmin match {
      case Some(usuario) =>
        val recursosFuturo = rcaDataAccessAdapter obtenerRecursos usuario.id
        recursosFuturo.map(_.map(x => resolveMessageRecursosClienteAdmin(usuario, x.filter(filtrarRecursosPerfilClienteAdmin(_, url.getOrElse(""))))))
      case _ =>
        Future.successful(Validation.success(ResponseMessage(Unauthorized, "Error Validando Token")))
    }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursosClienteAdmin(usuario: UsuarioEmpresarialAdmin, recursos: List[RecursoPerfilClienteAdmin]) = {
    recursos.isEmpty match {
      case true => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenClienteAdminMessage(usuario, None, "403.1")))
      case false =>
        val recurso = recursos.head

        recurso.filtro match {
          case Some(filtro) => ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenClienteAdminMessage(usuario, recurso.filtro, "403.2")))
          case None => ResponseMessage(OK, JsonUtil.toJson(usuario))

        }

    }
  }

  /**
   * Filtra el listado de recursos que concuerden con la url
   *
   * @param recurso recursos asociados al agente
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursosPerfilAgente(recurso: RecursoPerfilAgente, url: String): Boolean =
    filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

  /**
   * Filtra el listado de recursos que concuerden con la url
   *
   * @param recurso recursos asociados al cliente administrador
   * @param url la url a validar
   * @return
   */
  private def filtrarRecursosPerfilClienteAdmin(recurso: RecursoPerfilClienteAdmin, url: String): Boolean =
    filtrarRecursos(recurso.urlRecurso, recurso.acceso, url)

}

case class ForbiddenAgenteMessage(usuario: UsuarioEmpresarial, filtro: Option[String], code: String)
case class ForbiddenClienteAdminMessage(usuario: UsuarioEmpresarialAdmin, filtro: Option[String], code: String)