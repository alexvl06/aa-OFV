package portal.transaccional.autenticacion.service.drivers.ultimaContrasena

import co.com.alianza.persistence.entities.UltimaContrasena

import scala.concurrent.Future

/**
 * Created by hernando on 26/10/16.
 */
trait UltimaContrasenaRepository {

  def crearUltimaContrasena(ultimaContrasena: UltimaContrasena): Future[Int]

  def crearUltimaContrasenaAdmin(ultimaContrasena: UltimaContrasena): Future[Int]

  def crearUltimaContrasenaAgente(ultimaContrasena: UltimaContrasena): Future[Int]

  def getUltimasContrasenas(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]]

  def getUltimasContrasenasAdmin(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]]

  def getUltimasContrasenasAgente(idUsuario: Int, cantidad: Int): Future[Seq[UltimaContrasena]]

}
