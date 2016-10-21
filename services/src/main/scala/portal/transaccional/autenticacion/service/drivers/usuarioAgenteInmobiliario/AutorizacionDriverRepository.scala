package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions._
import co.com.alianza.persistence.entities.{UsuarioAgenteInmobiliario, UsuarioEmpresarialAdmin}
import co.com.alianza.persistence.repositories.UsuarioEmpresarialAdminRepository
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{AlianzaDAOs, UsuarioAgenteInmobDAO}
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.{DataAccessTranslator => ConstructorDataAccessTranslator}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by alexandra in 2016
 */
case class AutorizacionDriverRepository(sesionRepo: SesionRepository,
                                        alianzaDAO: AlianzaDAOs,
                                        usuarioInmobDAO : UsuarioAgenteInmobDAO)(implicit val ex: ExecutionContext) {

  def autorizar(token: String): Future[ValidacionAutorizacion] = {
    for {
      //_ <- validarToken(token)
      //_ <- sesionRepo.validarSesion(token)
      //_ <- sesionRepo.obtenerSesion(token)
      agente <- usuarioInmobDAO.getByToken(token)
      validacion <- obtener(None, agente)
    } yield validacion
  }

  def validarTokenInmobiliaria(token: String): Future[ValidacionAutorizacion] = {
    for {
    //_ <- validarToken(token)
    //_ <- sesionRepo.validarSesion(token)
    //_ <- sesionRepo.obtenerSesion(token)
      constructor <- alianzaDAO.getAdminTokenAgente(token)
      agente <- if (constructor.isEmpty) usuarioInmobDAO.getByToken(token) else Future.successful(None)
      validacion <- obtener(constructor, agente)
    } yield validacion
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  private def obtener(constructor: Option[(UsuarioEmpresarialAdmin, Int)],
                      agente: Option[UsuarioAgenteInmobiliario]): Future[ValidacionAutorizacion] = Future {
    (constructor, agente) match {
      case (Some(c), None) =>  Autorizado(JsonUtil.toJson(ConstructorDataAccessTranslator.entityToDto(c._1)))
      case (None, Some(a)) => AutorizadoAgente(JsonUtil.toJson(DataAccessTranslator.entityToDto(a)))
      case _ => NoAutorizado("El usuario no se encuentra logueado")
    }
  }
}