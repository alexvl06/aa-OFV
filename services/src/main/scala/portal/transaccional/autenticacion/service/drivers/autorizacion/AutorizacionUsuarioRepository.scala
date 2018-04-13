package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.exceptions.ValidacionAutorizacion
import co.com.alianza.infrastructure.dto.{ Usuario => UsuarioDTO }
import co.com.alianza.persistence.entities.Usuario

import scala.concurrent.Future

/**
 * Created by hernando on 27/07/16.
 */
trait AutorizacionUsuarioRepository {

  def autorizar(token: String, encriptedToken: String, url: String): Future[ValidacionAutorizacion]

  def invalidarToken(token: String, encriptedToken: String): Future[Int]

  /**OFV LOGIN FASE 1**/
  /**
   * Realiza validación de acceso a un recurso backend mediante el token para usuarios generales.
   * @param token Token generado.
   * @param url Recurso backend a validar.
   * @return Future[ValidacionAutorizacion]
   */
  def autorizarGeneral(token: String, url: String): Future[ValidacionAutorizacion]

  /**OFV LOGIN FASE 1**/
  /**
   * Invalidar token de acceso para usuarios generales
   * @param token
   * @param encriptedToken
   * @return
   */
  def invalidarTokenGeneral(token: String, encriptedToken: String): Future[Any]
}
