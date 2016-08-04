package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor.SesionUsuarioValidada
import co.com.alianza.exceptions.{ Autorizado, Prohibido, ValidacionAutorizacion, ValidacionException }
import co.com.alianza.infrastructure.messages.{ BuscarSesion, InvalidarSesion, ResponseMessage, ValidarSesion }
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.{ ObtenerEmpresaActor, ObtenerIps }
import co.com.alianza.infrastructure.dto.UsuarioEmpresarialAdmin
import co.com.alianza.persistence.entities.RecursoPerfilClienteAdmin
import co.com.alianza.util.json.JsonUtil
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.Recurso.RecursoRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO
import spray.http.StatusCodes._
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{ DataAccessTranslator, UsuarioEmpresarialAdminRepository }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by seven4n on 2016
 */
case class AutorizacionUsuarioEmpresarialAdminDriverRepository(adminRepo: UsuarioEmpresarialAdminRepository, sesionActor: ActorRef, alianzaDAO: AlianzaDAO,
    recursoRepo: RecursoRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioEmpresarialAdminRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizar(token: String, url: String, ip: String): Future[ValidacionAutorizacion] = {
    val encriptedToken = AesUtil.encriptarToken(token, "AutorizacionUsuarioEmpresarialAdminDriverRepository.autorizar")
    for {
      _ <- validarToken(encriptedToken)
      _ <- Future { (sesionActor ? ValidarSesion(encriptedToken)).mapTo[SesionUsuarioValidada] }
      sesion <- obtieneSesion(encriptedToken)
      adminEstado <- alianzaDAO.getByTokenAdmin(encriptedToken)
      _ <- validarEstadoEmpresa(adminEstado._2)
//      ips <- obtenerIps(sesion)
//      validarIp <- validarIps(ips, ip)
      recursos <- alianzaDAO.getAdminResources(adminEstado._1.id)
      result <- resolveMessageRecursos(DataAccessTranslator.entityToDto(adminEstado._1), recursos, url)
    } yield result
  }

  def invalidarToken(token: String): Future[Int] = {
    for {
      x <- adminRepo.invalidarToken(token)
      _ <- Future { sesionActor ? InvalidarSesion(token) }
    } yield x
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(AesUtil.desencriptarToken(token, "AutorizacionUsuarioEmpresarialAdminDriverRepository.validarToken")) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  private def obtieneSesion(token: String) = {
    val actor: Future[Any] = sesionActor ? BuscarSesion(token)
    actor flatMap {
      case Some(sesionActor: ActorRef) => Future.successful(sesionActor)
      case _ => Future.failed(ValidacionException("403.9", "Error sesión"))
    }
  }

  private def validarEstadoEmpresa(estado: Int): Future[ResponseMessage] = {
    val empresaActiva: Int = EstadosDeEmpresaEnum.activa.id
    estado match {
      case `empresaActiva` => Future.successful(ResponseMessage(OK, "Empresa Activa"))
      case _ => Future.failed(ValidacionException("401.23", "Error sesión"))
    }
  }

  private def obtenerIps(sesion: ActorRef): Future[List[String]] = {
    (sesion ? ObtenerEmpresaActor).flatMap {
      case Some(empresaSesionActor: ActorRef) =>
        (empresaSesionActor ? ObtenerIps).flatMap{
          case r : List[String] => Future.successful(r)
          case _ =>  Future.failed(ValidacionException("401.21YYY", "Error obtener ips"))
        }
      case _ => Future.failed(ValidacionException("401.21YYY", "Error sesión"))
    }
  }

  private def validarIps(ips: List[String], ip: String): Future[String] = {
    if (ips.contains(ip)) {
      Future.successful(ip)
    } else {
      Future.failed(ValidacionException("401.21XXX", "Error sesión"))
    }
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(adminDTO: UsuarioEmpresarialAdmin, recursos: Seq[RecursoPerfilClienteAdmin], url: String): Future[ValidacionAutorizacion] = Future {
    val recursosFiltro = recursoRepo.filtrarRecursosClienteAdmin(recursos, url)
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursos.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessageAdmin = ForbiddenMessageAdmin(adminDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(adminDTO)
            Autorizado(usuarioJson)
        }
    }
  }
}

case class ForbiddenMessageAdmin(usuario: UsuarioEmpresarialAdmin, filtro: Option[String])
