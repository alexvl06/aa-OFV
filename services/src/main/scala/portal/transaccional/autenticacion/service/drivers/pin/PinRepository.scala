package portal.transaccional.autenticacion.service.drivers.pin

import scala.concurrent.Future

/**
 * Created by hernando on 25/10/16.
 */
trait PinRepository {

  def validarPinUsuario(pin: String, funcionalidad: Int): Future[Boolean]

  def validarPinAdmin(pin: String, funcionalidad: Int): Future[Boolean]

  def validarPinAgente(pin: String): Future[Boolean]

  def cambioContrasenaUsuario(token: String, contrasena: String, ip: Option[String]): Future[Int]

  def cambioContrasenaAdmin(token: String, contrasena: String, ip: Option[String]): Future[Int]

  def cambioContrasenaAgente(token: String, contrasena: String): Future[Int]

}
