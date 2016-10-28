package portal.transaccional.autenticacion.service.drivers.ultimaContrasena

import co.com.alianza.persistence.entities.UltimaContrasena
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UltimaContrasenaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 26/10/16.
 */
case class UltimaContrasenaDriverRepository(ultimaContrasenaDAO: UltimaContrasenaDAOs, ultimaContrasenaAdminDAO: UltimaContrasenaDAOs,
    ultimaContrasenaAgenteDAO: UltimaContrasenaDAOs)(implicit val ex: ExecutionContext) extends UltimaContrasenaRepository {

  def crearUltimaContrasena(ultimaContrasena: UltimaContrasena): Future[Int] = {
    ultimaContrasenaDAO.create(ultimaContrasena)
  }

  def crearUltimaContrasenaAdmin(ultimaContrasena: UltimaContrasena): Future[Int] = {
    ultimaContrasenaAdminDAO.create(ultimaContrasena)
  }

  def crearUltimaContrasenaAgente(ultimaContrasena: UltimaContrasena): Future[Int] = {
    ultimaContrasenaAgenteDAO.create(ultimaContrasena)
  }

  def getUltimasContrasenas(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]] = {
    ultimaContrasenaDAO.getByAmount(idUsuario, cantidad)
  }

  def getUltimasContrasenasAdmin(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]] = {
    ultimaContrasenaAdminDAO.getByAmount(idUsuario, cantidad)
  }

  def getUltimasContrasenasAgente(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]] = {
    ultimaContrasenaAgenteDAO.getByAmount(idUsuario, cantidad)
  }

}
