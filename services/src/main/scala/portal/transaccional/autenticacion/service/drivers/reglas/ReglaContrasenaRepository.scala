package portal.transaccional.autenticacion.service.drivers.reglas

import co.com.alianza.persistence.entities.ReglaContrasena
import enumerations.PerfilesUsuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait ReglaContrasenaRepository {

  def getRegla(llave: String): Future[ReglaContrasena]

  def getReglas(): Future[Seq[ReglaContrasena]]

  def validarContrasenaReglasGenerales(idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario, contrasena: String): Future[Boolean]

  def validarContrasenaReglasAutorregistro(contrasena: String): Future[Boolean]

  def validarContrasenaReglasIngresoUsuario(contrasena: String): Future[Boolean]

}
