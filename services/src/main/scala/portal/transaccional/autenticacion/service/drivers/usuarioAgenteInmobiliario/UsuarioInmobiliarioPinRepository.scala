package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import akka.actor.ActorSystem
import co.com.alianza.microservices.MailMessage
import co.com.alianza.persistence.entities.{ Configuraciones, PinAgenteInmobiliario }
import com.typesafe.config.Config
import enumerations.EstadosPin.EstadoPin

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Define las funciones del repositorio de pin de los agentes inmobiliarios
 */
trait UsuarioInmobiliarioPinRepository {

  /**
   * Asocia el pin generado al agente inmobiliario y lo guarda en la base de datos
   *
   * @param pinAgente Pin a guardar
   * @return Un futuro con el identificador del pin asociado
   */
  def asociarPinAgente(pinAgente: PinAgenteInmobiliario): Future[Option[Int]]

  /**
   * Verifica la validez de un pin generado para un usuario agente inmobiliario
   *
   * @param hash Hash del pin a validar
   * @param ex   Contexto de ejecución del llamado
   * @return Un either con el pin valido
   */
  def validarPinAgente(hash: String)(implicit ex: ExecutionContext): Future[Either[EstadoPin, PinAgenteInmobiliario]]

  /**
   * Genera el pin de un agente inmobiliario
   *
   * @param configExpiracion Configuración del tiempo de expiración
   * @param idUsuario        Identificador del agente
   * @return Una instancia de la clase PinAgenteInmobiliario con el pin generado
   */
  def generarPinAgente(configExpiracion: Configuraciones, idUsuario: Int): PinAgenteInmobiliario

  /**
   * Genera el correo de activación del agente inmobiliario
   *
   * @param pin            Pin generado para el agente
   * @param caducidad      Tiempo de caducidad
   * @param identificacion Identificación de la empresa
   * @param usuario        Nombre de usuario del agente inmobiliario
   * @param correo         Correo del agente inmobiliario
   * @return Una instancia de la clase MailMessage con el contenido del correo a enviar
   */
  def generarCorreoActivacion(pin: String, caducidad: Int, identificacion: String, usuario: String,
    correo: String)(implicit config: Config): MailMessage

  /**
    * Genera el correo de reinicio de contraseña del agente inmobiliario
    *
    * @param pin            Pin generado para el agente
    * @param caducidad      Tiempo de caducidad
    * @param usuario        Nombre de usuario del agente inmobiliario
    * @param correo         Correo del agente inmobiliario
    * @return Una instancia de la clase MailMessage con el contenido del correo a enviar
    */
  def generarCorreoReinicio(pin: String, caducidad: Int, usuario: String,
                            correo: String)(implicit config: Config): MailMessage

  /**
   * Envía un correo de forma asyncrona
   *
   * @param correo       Correo a enviar
   * @param actorySystem Sistema de actores para ejecutar el cliente smtp
   */
  def enviarEmail(correo: MailMessage)(implicit actorySystem: ActorSystem): Unit

  /**
   * Elimina un pin asociado a un agente
   *
   * @param pin Hash del pin a eliminar
   * @return Un futuro con la cantidad de filas modificadas
   */
  def eliminarPinAgente(pin: String): Future[Int]
}
