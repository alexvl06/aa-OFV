package portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by s4n in 2016
 */
trait ContrasenaAgenteInmobiliarioRepository {

  /**
   * Actualiza la contraseña caducada de un agente inmobiliario
   *
   * @param token         Token de tipo caducidad de contraseña, encriptado.
   * @param pw_actual     Contraseña sin encriptar que se encuentra registrada en base de datos.
   * @param pw_nuevo      Contraseña nueva sin encriptar, por la que se emplezara pw_actual.
   * @param idUsuario     id del Usuario al cual se le registrara la nueva clave.
   * @return              Si es valida la contraseña actual, y cumple con las validaciones la nueva contraseña,
   *                      retorna el idUsuario al cual se le actualizó la contraseña. De no ser asi, retorna un futuro fallido,
   *                      con un mensaje de error acorde a la validación fallida.
   */
  def actualizarContrasenaCaducada(token: Option[String], pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]): Future[Int]

  /**
    * Actualiza la contraseña de un agente inmobiliario dado un hash asociado
    *
    * @param pinHash          Pin asociado al agente
    * @param contrasena       Contraseña nueva a definir
    * @param contrasenaActual OPCIONAL - Contraseña actual
    * @return Si cumple con las validaciones la nueva contraseña, retorna el idUsuario al cual se le actualizó la contraseña.
    *         De no ser asi, retorna un futuro fallido con un mensaje de error acorde a la validación fallida.
    */
  def actualizarContrasenaPin(pinHash: String, contrasena: String, contrasenaActual: Option[String]): Future[Int]
}
