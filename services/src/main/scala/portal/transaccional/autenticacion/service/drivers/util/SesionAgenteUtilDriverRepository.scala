package portal.transaccional.autenticacion.service.drivers.util

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.messages.ResponseMessage
import co.com.alianza.persistence.entities.UsuarioAgente
import co.com.alianza.util.token.Token
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.autenticacion.service.drivers.sesion.SesionDriverRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import spray.http.StatusCodes._

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
case class SesionAgenteUtilDriverRepository[E <: UsuarioAgente](agenteRepo : UsuarioEmpresarialRepository[E] , sesionRepo: SesionDriverRepository) extends SesionAgenteUtilRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- agenteRepo.invalidarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }

  def validarToken(token: String): Future[Boolean] = {
    Token.autorizarToken(token) match {
      case true => Future.successful(true)
      case false => Future.failed(ValidacionException("401.24", "Error token"))
    }
  }

  def validarEstadoEmpresa(estado: Int): Future[ResponseMessage] = {
    val empresaActiva: Int = EstadosDeEmpresaEnum.activa.id
    estado match {
      case `empresaActiva` => Future.successful(ResponseMessage(OK, "Empresa Activa"))
      case _ => Future.failed(ValidacionException("401.23", "Error sesión"))
    }
  }
}
