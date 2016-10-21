package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions._
import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ AlianzaDAOs, UsuarioAgenteInmobDAO }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra in 2016
 */
case class AutorizacionDriverRepository(sesionRepo: SesionRepository, alianzaDAO: AlianzaDAOs, usuarioDAO : UsuarioAgenteInmobDAO)(implicit val ex:
ExecutionContext) {

  def autorizar(token: String): Future[ValidacionAutorizacion] = {
    for {
      //_ <- validarToken(token)
      //_ <- sesionRepo.validarSesion(token)
      //_ <- sesionRepo.obtenerSesion(token)
      agente <- usuarioDAO.getByToken(token)
      validacion <- obtener(agente)
    } yield validacion
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  private def obtener(agente: Option[UsuarioAgenteInmobiliario]): Future[ValidacionAutorizacion] = Future {
    agente match {
      case Some(e)=> AutorizadoAgente(JsonUtil.toJson(DataAccessTranslator.entityToDto(e)))
      case None => NoAutorizado("El usuario no se encuentra logueado")
    }
  }

}