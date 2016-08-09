package portal.transaccional.autenticacion.service.drivers.autorizacion

import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.drivers.usuarioComercialAdmin.UsuarioComercialAdminRepository

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by alexandra on 8/08/16.
 */
case class AutorizacionUsuarioComercialAdminDriverRepository(sesionRepo: SesionRepository, usuarioRepo: UsuarioComercialAdminRepository)(implicit val ex:
ExecutionContext) extends AutorizacionUsuarioComercialAdminRepository {

  def invalidarToken(token: String, encriptedToken: String): Future[Int] = {
    for {
      x <- usuarioRepo.eliminarToken(encriptedToken)
      _ <- sesionRepo.eliminarSesion(token)
    } yield x
  }

}
