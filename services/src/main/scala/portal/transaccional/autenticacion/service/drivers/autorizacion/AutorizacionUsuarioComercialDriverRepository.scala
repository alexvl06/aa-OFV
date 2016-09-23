package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import co.com.alianza.exceptions.{ AutorizadoComercial, NoAutorizado, ValidacionAutorizacion }
import co.com.alianza.infrastructure.dto.UsuarioComercialDTO
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

  def invalidarTokenSAC(token: String, encriptedToken: String): Future[Int] = {
    usuarioRepo.eliminarToken(encriptedToken)
  }

  def autorizarFiduciaria(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialFiduciaria)
    } yield usuario
  }

  def autorizarValores(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token)
      validarSesion <- sesionRepo.validarSesion(token)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialValores)
    } yield usuario
  }

  def autorizarSAC(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion] = {
    for {
      validar <- validarToken(token, false)
      usuarioOption <- usuarioRepo.getByToken(encriptedToken)
      usuario <- validarUsario(usuarioOption, TiposCliente.comercialSAC)
    } yield usuario
  }

  private def validarUsario(usuarioOption: Option[UsuarioComercial], tipoCliente: TiposCliente): Future[ValidacionAutorizacion] = {
    usuarioOption match {
      case Some(usuario: UsuarioComercial) =>
        val usuarioDTO: UsuarioComercialDTO = UsuarioComercialDTO(tipoCliente, usuario.id, usuario.usuario)
        val usuarioJson: String = JsonUtil.toJson(usuarioDTO)
        Future.successful(AutorizadoComercial(usuarioJson))
      case _ => Future.failed(NoAutorizado("usuario no encontrado"))
    }
  }

  private def validarToken(token: String, validarExpiracionToken: Boolean = true): Future[Boolean] = {
    Token.autorizarToken(token, validarExpiracionToken) match {
      case true => Future.successful(true)
      case _ => Future.failed(NoAutorizado("Token erróneo"))
    }
  }

}
