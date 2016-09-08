package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.{ AutorizadoComercial, Autorizado, NoAutorizado, ValidacionAutorizacion }
import co.com.alianza.persistence.entities.UsuarioComercial
import co.com.alianza.util.json.JsonUtil
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n 2016
 */
case class AutorizacionUsuarioComercialDriverRepository(sesionRepo: SesionRepository, usuarioRepo: UsuarioComercialRepository)(implicit val ex: ExecutionContext) extends AutorizacionUsuarioComercialRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.eliminarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }

  def autorizar(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption)
    } yield usuario
  }

  private def validarUsario(usuarioOption: Option[UsuarioComercial]): Future[ValidacionAutorizacion] = {
    usuarioOption match {
      case Some(usuario: UsuarioComercial) =>
        val usuarioJson: String = JsonUtil.toJson(usuario)
        Future.successful(AutorizadoComercial(usuarioJson))
      case _ => Future.failed(NoAutorizado("usuario no existe"))
    }
  }

  private def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }

}
