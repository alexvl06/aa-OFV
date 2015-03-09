package co.com.alianza.domain.aggregates.autenticacion

import akka.pattern.ask
import akka.actor.ActorRef

import co.com.alianza.util.token.Token
import co.com.alianza.util.transformers.ValidationT
import co.com.alianza.infrastructure.messages._
import co.com.alianza.domain.aggregates.autenticacion.errores._
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.infrastructure.dto._
import co.com.alianza.infrastructure.anticorruption.recursosAgenteEmpresarial.{DataAccessAdapter => raDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.recursosClienteAdmin.{DataAccessAdapter => rcaDataAccessAdapter}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => usDataAdapter}
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.app.MainActors

import scala.concurrent.Future
import scalaz.std.AllInstances._
import scalaz.Validation
import scala.util.{Success => sSuccess, Failure => sFailure}
import scalaz.{Failure => zFailure, Success => zSuccess, Validation}

import spray.http.StatusCodes._

/**
 * Created by manuel on 16/12/14.
 */
class AutorizacionUsuarioEmpresarialActor extends AutorizacionActor with ValidacionesAutenticacionUsuarioEmpresarial {

  override def receive = {

    case message: AutorizarUsuarioEmpresarialMessage =>
      val currentSender = sender()
      val future = (for {
        token <- ValidationT(validarToken(message.token))
        sesion <- ValidationT(obtieneSesion(token))
        usuarioOption <- ValidationT(obtieneUsuarioEmpresarial(token))
        validUs <- ValidationT(validarUsuario(usuarioOption))
        validacionIp <- ValidationT(validarIpEmpresa(sesion, message.ip))
        validacionHorario <- ValidationT(validarHorarioEmpresa(sesion))
        result <- ValidationT(autorizarRecursoAgente(usuarioOption, message.url))
      } yield {
        result
      }).run
      resuelveAutorizacionAgente(future, currentSender)

    case message: AutorizarUsuarioEmpresarialAdminMessage =>
      val currentSender = sender()
      val future = (for {
        usuarioOption <- ValidationT(validarTokenAdmin(message.token))
        result <- ValidationT(validarRecursoClienteAdmin(usuarioOption, message.url))
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

  private def validarToken(token: String) : Future[Validation[ErrorAutorizacion, String]] =
    Token.autorizarToken(token) match {
      case true =>
        Future.successful(Validation.success(token))
      case false =>
        Future.successful(Validation.failure(TokenInvalido()))
    }


  private def obtieneSesion(token: String) : Future[Validation[ErrorAutorizacion, ActorRef]] =
    MainActors.sesionActorSupervisor ? BuscarSesion(token) map {
      case Some(sesionActor: ActorRef) => Validation.success(sesionActor)
      case None => Validation.failure(ErrorSesionNoEncontrada())
    }

  private def obtieneUsuarioEmpresarial(token: String): Future[Validation[ErrorAutorizacion, Option[UsuarioEmpresarial]]] =
    usDataAdapter.obtenerUsuarioEmpresarialToken(token) map {
      _.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) }
    }

  private def validarIpEmpresa(sesion: ActorRef, ip: String) : Future[Validation[ErrorAutorizacion, String]] =
    sesion ? ObtenerEmpresaActor flatMap {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ? ObtenerIps map {
          case ips : List[String] if ips.contains(ip) => Validation.success(ip)
          case ips : List[String] if ips.isEmpty || !ips.contains(ip) =>
            sesion ! ExpirarSesion()
            Validation.failure(ErrorSesionIpInvalida(ip));
        }
      case None =>
        log error ("+++No encontrado empresa actor.");
        Future.successful(Validation.failure(ErrorSesionIpInvalida(ip)))
    }

  private def validarHorarioEmpresa(sesion: ActorRef) : Future[Validation[ErrorAutorizacion, Unit]] =
    sesion ? ObtenerEmpresaActor flatMap {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ? ObtenerHorario flatMap {
          case horario: Option[HorarioEmpresa] =>
            validarHorarioEmpresa(horario) map {
              case zSuccess(true) =>
                Validation success((): Unit)
              case zSuccess(false) =>
                sesion ! ExpirarSesion()
                Validation failure ErrorSesionHorarioInvalido()
              case zFailure(error) =>
                sesion ! ExpirarSesion()
                Validation failure ErrorSesionHorarioInvalido()
            }
        }
      case None =>
        log error ("+++No encontrado empresa actor.");
        Future.successful(Validation failure ErrorSesionHorarioInvalido())
    }

  import scalaz.Validation.FlatMap._
  private def autorizarRecursoAgente(agente: Option[UsuarioEmpresarial], url: Option[String]) : Future[Validation[ErrorAutorizacion, UsuarioEmpresarial]] =
    raDataAccessAdapter obtenerRecursos agente.get.id map {
      _.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) } flatMap { x =>
        resolveAutorizacionRecursosAgente(agente.get, x.filter(filtrarRecursosPerfilAgente(_, url.getOrElse(""))))
      }
    }

  private def resolveAutorizacionRecursosAgente(usuario: UsuarioEmpresarial, recursos: List[RecursoPerfilAgente]) =
    recursos.isEmpty match {
      case true => Validation.failure(RecursoInexistente(usuario))
      case false =>
        recursos.head.filtro match {
          case Some(filtro) => Validation.failure(RecursoProhibido(usuario))
          case None => Validation.success(usuario)
        }
    }

  private def resuelveAutorizacionAgente(futureValidation: Future[Validation[ErrorAutorizacion, UsuarioEmpresarial]], originalSender: ActorRef) =
    futureValidation onComplete {
      case sFailure(error) => originalSender ! error
      case sSuccess(resp) => resp match {
        case zSuccess(usuario) => originalSender ! ResponseMessage(OK, JsonUtil.toJson(usuario))
        case zFailure(errorAutorizacion) => errorAutorizacion match {
          case ErrorSesionNoEncontrada() => originalSender ! ResponseMessage(Unauthorized, "Error Validando Token")
          case TokenInvalido() => originalSender ! ResponseMessage(Unauthorized, "Error Validando Token")
          case ErrorPersistenciaAutorizacion(_, ep1) => originalSender ! ep1
          case RecursoInexistente(usuario) =>
            originalSender ! ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, None, "403.1")))
          case RecursoProhibido(usuario) =>
            originalSender ! ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, None, "403.2")))
          case e @ ErrorSesionIpInvalida(_) => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case e @ ErrorSesionHorarioInvalido() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case a => log error "***+ Error autorización: "+a.msg; originalSender ! ResponseMessage(Forbidden, errorAutorizacion.msg)
        }
      }
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
  private def validarUsuario(usuarioOp: Option[Any]) : Future[Validation[ErrorAutorizacion, Unit]] = {
    usuarioOp match {
      case None => Future.successful(Validation.failure(TokenInvalido()))
      case Some(us) => Future.successful(Validation.success(():Unit))
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