package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.util.Timeout
import co.com.alianza.exceptions._
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.persistence.entities.{ RecursoPerfil, Usuario }
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.{ DataAccessTranslator, UsuarioRepository }
import portal.transaccional.autenticacion.service.web.autorizacion.{ AuditityUser, UserResponseAuthGen }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAOs

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import spray.json._
import DefaultJsonProtocol._

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioDriverRepository(usuarioRepo: UsuarioRepository, recursoRepo: RecursoRepository, sesionRepo: SesionRepository,
    /**OFV LOGIN FASE 1**/
    alianzaDAOs: AlianzaDAOs)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizar(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
      recursos <- recursoRepo.obtenerRecursos(usuario.id.get)
      validarRecurso <- resolveMessageRecursos(usuario, recursos, url)
    } yield validarRecurso
  }

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.invalidarToken(encriptedToken)
      n <- sesionRepo.eliminarSesion(token)
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
      case _ => Future.failed(NoAutorizado("Token err??neo"))
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(usuario: Usuario, recursos: Seq[RecursoPerfil], url: String): Future[ValidacionAutorizacion] = Future {
    //si la url es "", viene desde el mismo componente, por lo tanto no hay que hacer filtro alguno
    val recursosFiltro: Seq[RecursoPerfil] = {
      if (url.nonEmpty) recursoRepo.filtrarRecursos(recursos, url)
      else Seq(RecursoPerfil(0, url, false, None))
    }
    val usuarioDTO: UsuarioDTO = DataAccessTranslator.entityToDto(usuario)
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursosFiltro.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessage = ForbiddenMessage(usuarioDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(usuarioDTO)
            Autorizado(usuarioJson)
        }
    }
  }

  /**OFV LOGIN FASE 1**/
  /**
   * Realiza validaci??n de acceso a un recurso backend mediante el token para usuarios generales.
   * @param token Token generado.
   * @param url Recurso backend a validar.
   * @return Future[ValidacionAutorizacion]
   */
  def autorizarGeneral(token: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      existe <- alianzaDAOs.validResourceByPerfil(getProfile(token), url)
      validarRecursoGen <- validarRecursoGen(existe, url, token)
    } yield validarRecursoGen
  }
  /**OFV LOGIN FASE 1**/
  /**
   * Invalidar token de acceso para usuarios generales
   * @param token
   * @param encriptedToken
   * @return
   */
  def invalidarTokenGeneral(token: String, encriptedToken: String): Future[Any] = {
    for {
      n <- sesionRepo.eliminarSesion(token)
    } yield n
  }

  /**OFV LOGIN FASE 1**/
  private def getProfile(token: String): Int = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    nToken.getCustomClaim("tipoCliente").toString.toInt
  }

  /**OFV LOGIN FASE 1**/
  private def validarRecursoGen(validacion: Boolean, url: String, token: String): Future[ValidacionAutorizacion] = Future {
    validacion match {
      case false => Prohibido("403.2", "{\"url\":\"" + url + "\"}")
      case true => Autorizado(JsonUtil.toJson(this.getTokenData(token)))
    }
  }

  private def getTokenData(token: String): UserResponseAuthGen = {
    val nToken = Token.getToken(token).getJWTClaimsSet
    val tipoCliente = nToken.getCustomClaim("tipoCliente").toString
    //TODO: el nit no lo pide si es tipo comercial
    val idPerfil = nToken.getCustomClaim("tipoCliente").toString
    val idUsuario = nToken.getCustomClaim("idUsuario").toString
    val email = nToken.getCustomClaim("correo").toString
    val lastEntry = nToken.getCustomClaim("ultimaFechaIngreso").toString
    val nitType = nToken.getCustomClaim("tipoIdentificacion").toString
    UserResponseAuthGen(idPerfil, idUsuario)
  }
}

case class ForbiddenMessage(usuario: UsuarioDTO, filtro: Option[String])