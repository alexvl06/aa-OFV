package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions.{ Autorizado, Prohibido, ValidacionAutorizacion, ValidacionException }
import co.com.alianza.infrastructure.dto.UsuarioAgenteInmobiliario
import co.com.alianza.persistence.entities.RecursoBackendInmobiliario
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.recurso.RecursoRepository
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.AlianzaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra in 2016
 */
case class AutorizacionDriverRepository(sesionRepo: SesionRepository, alianzaDAO: AlianzaDAOs, recursoRepo: RecursoRepository)
  (implicit val ex: ExecutionContext) extends AutorizacionRepository {

  def autorizar(token: String, encriptedToken: String, url: Option[String], ip: String): Future[ValidacionAutorizacion] = {
    for {
      _ <- validarToken(token)
      _ <- sesionRepo.validarSesion(token)
      _ <- sesionRepo.obtenerSesion(token)
      agente <- alianzaDAO.getByTokenAgenteInmobiliario(encriptedToken)
      recursos <- alianzaDAO.get5(agente.id)
      validacion <- filtrarUrl(DataAccessTranslator.entityToDto(agente), recursos, url.getOrElse(""))
    } yield validacion
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  private def filtrarUrl(agente: UsuarioAgenteInmobiliario, recursos: Seq[RecursoBackendInmobiliario], url: String): Future[ValidacionAutorizacion] = Future {

    val recursosFiltro: Seq[RecursoBackendInmobiliario] = recursoRepo.filtrarRecursoAgenteInmobiliario(recursos, url)

    if (recursosFiltro.nonEmpty){
      Autorizado(JsonUtil.toJson(agente))
    } else {
      val usuarioForbidden = ForbiddenMessageAgenteInmob(agente, None)
      Prohibido("403.1", JsonUtil.toJson(usuarioForbidden))
    }
  }

}

case class ForbiddenMessageAgenteInmob(usuario: UsuarioAgenteInmobiliario, filtro: Option[String])