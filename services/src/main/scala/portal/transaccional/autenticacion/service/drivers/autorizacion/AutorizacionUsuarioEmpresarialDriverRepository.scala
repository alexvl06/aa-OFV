package portal.transaccional.autenticacion.service.drivers.autorizacion

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import co.com.alianza.domain.aggregates.autenticacion.{ ObtenerEmpresaActor, ObtenerIps }
import co.com.alianza.exceptions.{ Autorizado, Prohibido, ValidacionAutorizacion, ValidacionException }
import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.persistence.entities.{ RecursoPerfilAgente, UsuarioEmpresarial => UsuarioEmpresarialE }
import co.com.alianza.util.json.JsonUtil
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.{ DataAccessTranslator, UsuarioEmpresarialRepository }
import portal.transaccional.autenticacion.service.drivers.util.{ SesionAgenteUtilDriverRepository, SesionAgenteUtilRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAO

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n on 2016
 */
case class AutorizacionUsuarioEmpresarialDriverRepository(agenteRepo: UsuarioEmpresarialRepository[UsuarioEmpresarialE], alianzaDAO: AlianzaDAO,
  sesionRepo: SesionDriverRepository, recursoRepo: RecursoRepository, sesionUtilRepo : SesionAgenteUtilRepository)(implicit val ex: ExecutionContext)
  extends AutorizacionUsuarioEmpresarialRepository {

  implicit val timeout = Timeout(5.seconds)

  def autorizar(token: String, encriptedToken: String, url: String, ip: String): Future[ValidacionAutorizacion] = {
    for {
      _ <- sesionUtilRepo.validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      sesion <- sesionRepo.obtenerSesion(token)
      agenteEstado <- alianzaDAO.getByTokenAgente(encriptedToken)
      _ <- sesionUtilRepo.validarEstadoEmpresa(agenteEstado._2)
      ips <- obtenerIps(sesion)
      validarIp <- validarIps(ips, ip)
      recursos <- alianzaDAO.getAgenteResources(agenteEstado._1.id)
      result <- resolveMessageRecursos(DataAccessTranslator.entityToDto(agenteEstado._1), recursos, url)
    } yield result
  }

  /**
   * De acuerdo si la lista tiene contenido retorna un ResponseMessage
   *
   * @param recursos Listado de recursos
   * @return
   */
  private def resolveMessageRecursos(agenteDTO: UsuarioEmpresarial, recursos: Seq[RecursoPerfilAgente], url: String): Future[ValidacionAutorizacion] = Future {
    val recursosFiltro = recursoRepo.filtrarRecursosAgente(recursos, url)
    recursosFiltro.nonEmpty match {
      case false =>
        val usuarioForbidden: ForbiddenMessageAgente = ForbiddenMessageAgente(agenteDTO, None)
        Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
      case true =>
        recursos.head.filtro match {
          case filtro @ Some(_) =>
            val usuarioForbidden: ForbiddenMessageAgente = ForbiddenMessageAgente(agenteDTO, filtro)
            Prohibido("403.2", JsonUtil.toJson(usuarioForbidden))
          case None =>
            val usuarioJson: String = JsonUtil.toJson(agenteDTO)
            Autorizado(usuarioJson)
        }
    }
  }

  private def obtenerIps(sesion: ActorRef): Future[List[String]] = {
    (sesion ? ObtenerEmpresaActor).flatMap {
      case Some(empresaSesionActor: ActorRef) => (empresaSesionActor ? ObtenerIps).mapTo[List[String]]
      case None => Future.failed(ValidacionException("401.21", "Error sesión"))
      case _ => Future.failed(ValidacionException("401.21", "esta devolviendo otra cosa"))
    }
  }

  private def validarIps(ips: List[String], ip: String): Future[String] = {
    if (ips.contains(ip)) {
      Future.successful(ip)
    } else {
      Future.failed(ValidacionException("401.21 -- ", "Error sesión"))
    }
  }

}

case class ForbiddenMessageAgente(usuario: UsuarioEmpresarial, filtro: Option[String])
