package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.{ AutorizadoComercialAdmin, Autorizado, NoAutorizado, ValidacionAutorizacion }
import co.com.alianza.persistence.entities.UsuarioComercialAdmin
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 2016
 */
case class AutorizacionUsuarioComercialAdminDriverRepository(usuarioRepo: UsuarioComercialAdminRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioComercialAdminRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.eliminarToken(encriptedToken)
    } yield x
  }

  def autorizar(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
    } yield usuario
  }

  private def validarUsario(usuarioOption: Option[UsuarioComercialAdmin]): Future[ValidacionAutorizacion] = {
    usuarioOption match {
      case Some(usuario: UsuarioComercialAdmin) =>
        val usuarioJson: String = JsonUtil.toJson(usuario)
        Future.successful(AutorizadoComercialAdmin(usuarioJson))
      case _ => Future.failed(NoAutorizado("usuario no existe"))
    }
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token, false) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }

}
