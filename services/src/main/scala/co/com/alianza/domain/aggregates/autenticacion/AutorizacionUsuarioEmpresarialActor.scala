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
import co.com.alianza.exceptions.{TechnicalLevel, PersistenceException}
import co.com.alianza.app.MainActors
import enumerations.EstadosEmpresaEnum
import enumerations.empresa.EstadosDeEmpresaEnum

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
        token <- ValidationT(validarToken(message))
        sesion <- ValidationT(obtieneSesion(message.token))
        tuplaUsuarioOptionEstadoEmpresa <- ValidationT(obtieneUsuarioEmpresarial(message.token))
        validUs <- ValidationT(validarUsuario(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._1) }))
        validacionEstadoEmpresa <- ValidationT(validarEstadoEmpresa(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._2) }))
        validacionIp <- ValidationT(validarIpEmpresa(sesion, message.ip))
        validacionHorario <- ValidationT(validarHorarioEmpresa(sesion))
        result <- ValidationT(autorizarRecursoAgente(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._1) }, message.url))
      } yield {
        result
      }).run
      resuelveAutorizacionAgente(future, currentSender)

    case message: AutorizarUsuarioEmpresarialAdminMessage =>
      val currentSender = sender()
      val future = (for {
        tuplaUsuarioOptionEstadoEmpresa <- ValidationT(validarTokenAdmin(message.token))
        validacionEstadoEmpresa <- ValidationT(validarEstadoEmpresa(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._2) }))
        result <- ValidationT(validarRecursoClienteAdmin(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._1) }, message.url))
      } yield {
        result
      }).run

      resuelveAutorizacionClienteAdmin(future, currentSender)
  }

  /**
   * Realiza la validación del Token, llamando a [[Token.autorizarToken]]
   * Retorna un futuro con un Validationm, donde el caso de contiene el Option[Usuario]
   *
   *
   * @param message El token para realizar validación
   */
  private def validarToken(message: AutorizarUsuarioEmpresarialMessage): Future[Validation[ErrorAutorizacion, Option[UsuarioEmpresarial]]] = {
    Token.autorizarToken(message.token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialToken(message.token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarial]]] = x.map { userOpt =>
            guardaTokenCache( userOpt match { case None => None case _ => Some(userOpt.get._1) }, message)
          }
          co.com.alianza.util.transformers.Validation.sequence(y).map(_.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) })
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
  private def validarTokenAdmin(token: String): Future[Validation[ErrorAutorizacion, Option[(UsuarioEmpresarialAdmin, Int)]]] = {
    Token.autorizarToken(token) match {
      case true =>
        usDataAdapter.obtenerUsuarioEmpresarialAdminToken(token).flatMap { x =>
          val y: Validation[PersistenceException, Future[Option[(UsuarioEmpresarialAdmin, Int)]]] = x.map { userOpt =>
            guardaTokenAdminCache(userOpt, token)
          }
          co.com.alianza.util.transformers.Validation.sequence(y).map(_.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) })
        }
      case false =>
        Future.successful(Validation.success(None))
    }
  }

  private def obtieneSesion(token: String) : Future[Validation[ErrorAutorizacion, ActorRef]] =
    MainActors.sesionActorSupervisor ? BuscarSesion(token) map {
      case Some(sesionActor: ActorRef) =>
        Validation.success(sesionActor)
      case None =>
        Validation.failure(ErrorSesionNoEncontrada())
    }

  private def obtieneUsuarioEmpresarial(token: String): Future[Validation[ErrorAutorizacion, Option[(UsuarioEmpresarial, Int)]]] =
    usDataAdapter.obtenerUsuarioEmpresarialToken(token) map {
      _.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) }
    }

  private def validarIpEmpresa(sesion: ActorRef, ip: String) : Future[Validation[ErrorAutorizacion, String]] =
    sesion ? ObtenerEmpresaActor flatMap {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ? ObtenerIps map {
          case ips : List[String] if ips.contains(ip) => Validation.success(ip)
          case ips : List[String] if ips.isEmpty || !ips.contains(ip) =>
            Validation.failure(ErrorSesionIpInvalida(ip));
        }
      case None =>
        Future.successful(Validation.failure(ErrorSesionIpInvalida(ip)))
    }

  private def validarEstadoEmpresa( optionEstadoEmpresa: Option[Int] ) : Future[Validation[ErrorAutorizacion, ResponseMessage]] = {
    val empresaActiva: Int = EstadosDeEmpresaEnum.activa.id
    optionEstadoEmpresa match {
      case None => Future.successful(Validation.success(ResponseMessage(Unauthorized, TokenInvalido().msg)))
      case Some(estadoEmpresa) => estadoEmpresa match {
        case `empresaActiva` => Future.successful(Validation.success(ResponseMessage(OK, "Empresa Activa")))
        case _ => Future.successful(Validation.failure(ErrorSesionEstadoEmpresaDenegado()))
      }
    }
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
                Validation failure ErrorSesionHorarioInvalido()
              case zFailure(error) =>
                Validation failure ErrorSesionHorarioInvalido()
            }
        }
      case None =>
        Future.successful(Validation failure ErrorSesionHorarioInvalido())
    }

  import scalaz.Validation.FlatMap._
  private def autorizarRecursoAgente(agente: Option[UsuarioEmpresarial], url: Option[String]) : Future[Validation[ErrorAutorizacion, UsuarioEmpresarial]] =
    raDataAccessAdapter obtenerRecursos agente.get.id map {
      _.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) } flatMap { x =>
        resolveAutorizacionRecursosAgente(agente.get, x.filter(filtrarRecursosPerfilAgente(_, url.getOrElse(""))))
      }
    }

  private def resolveAutorizacionRecursosAgente(usuario: UsuarioEmpresarial, recursos: List[RecursoPerfilAgente]) ={
    recursos.isEmpty match {
      case true =>
        Validation.failure(RecursoInexistente(usuario))
      case false =>
        recursos.head.filtro match {
          case f @ Some(_) =>
            Validation.failure(RecursoRestringido(usuario, f))
          case None =>
            Validation.success(usuario)
        }
    }
  }

  private def resuelveAutorizacionAgente(futureValidation: Future[Validation[ErrorAutorizacion, UsuarioEmpresarial]], originalSender: ActorRef) =
    futureValidation onComplete {
      case sFailure(error) => originalSender ! error
      case sSuccess(resp) => resp match {
        case zSuccess(usuario) =>
          originalSender ! ResponseMessage(OK, JsonUtil.toJson(usuario))
        case zFailure(errorAutorizacion) =>
          errorAutorizacion match {
          case e @ ErrorSesionNoEncontrada() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case e @ TokenInvalido() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case ErrorPersistenciaAutorizacion(_, ep1) => originalSender ! ep1
          case RecursoInexistente(usuario) =>
            originalSender ! ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, None, "403.1")))
          case RecursoRestringido(usuario, filtro) =>
            originalSender ! ResponseMessage(Forbidden, JsonUtil.toJson(ForbiddenAgenteMessage(usuario, filtro, "403.2")))
          case e @ ErrorSesionIpInvalida(_) => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case e @ ErrorSesionHorarioInvalido() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case e @ ErrorSesionEstadoEmpresaDenegado() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case a => log error "***+ Error autorización: "+a.msg; originalSender ! ResponseMessage(Forbidden, errorAutorizacion.msg)
        }
      }
    }

  private def resuelveAutorizacionClienteAdmin(futureValidation: Future[Validation[ErrorAutorizacion, ResponseMessage]], originalSender: ActorRef) =
    futureValidation onComplete {
      case sFailure(error) => originalSender ! error
      case sSuccess(resp) => resp match {
        case zSuccess(usuario) =>
          originalSender ! usuario
        case zFailure(errorAutorizacion) => errorAutorizacion match {
          case e @ ErrorSesionNoEncontrada() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case e @ TokenInvalido() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case ErrorPersistenciaAutorizacion(_, ep1) => originalSender ! ep1
          case e @ ErrorSesionEstadoEmpresaDenegado() => originalSender ! ResponseMessage(Unauthorized, e.msg)
          case _ => originalSender ! errorAutorizacion
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
  private def guardaTokenAdminCache(usuarioOption: Option[(UsuarioEmpresarialAdmin, Int)], token: String): Future[Option[(UsuarioEmpresarialAdmin, Int)]] = {

    val validacionSesion: Future[Boolean] = ask(MainActors.sesionActorSupervisor, ValidarSesion(token)).mapTo[Boolean]
    validacionSesion.map {
      case true => usuarioOption.map(usuario => (usuario._1.copy(contrasena = None), usuario._2))
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

  private def validarRecursoClienteAdmin(clienteAdmin: Option[UsuarioEmpresarialAdmin], url: Option[String]): Future[Validation[ErrorAutorizacion, ResponseMessage]] =
    clienteAdmin match {
      case Some(usuario) =>
        val recursosFuturo = rcaDataAccessAdapter obtenerRecursos usuario.id
        recursosFuturo.map(_.map(x => resolveMessageRecursosClienteAdmin(usuario, x.filter(filtrarRecursosPerfilClienteAdmin(_, url.getOrElse("")))))).map(_.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) })
      case _ =>
        Future.successful(Validation.success(ResponseMessage(Unauthorized, TokenInvalido().msg)))
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