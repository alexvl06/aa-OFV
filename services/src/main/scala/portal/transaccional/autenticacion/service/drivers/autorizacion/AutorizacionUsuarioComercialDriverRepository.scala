package portal.transaccional.autenticacion.service.drivers.autorizacion

import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercial.UsuarioComercialRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n 2016
 */
case class AutorizacionUsuarioComercialDriverRepository (sesionRepo : SesionRepository, usuarioRepo : UsuarioComercialRepository ) (implicit val ex:
ExecutionContext)extends AutorizacionUsuarioComercialRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.eliminarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }
}
