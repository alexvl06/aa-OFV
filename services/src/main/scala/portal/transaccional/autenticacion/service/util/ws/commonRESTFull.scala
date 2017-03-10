package portal.transaccional.autenticacion.service.util.ws

import co.com.alianza.infrastructure.messages.ErrorMessage
import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
 * Created by s4npr02 on 13/06/16.
 */

import co.com.alianza.util.json.MarshallableImplicits._

/**
 * Define funciones y valores comunes para los servicios web RESTful.
 */
trait CommonRESTFul extends CommonService with SprayJsonSupport with DefaultJsonProtocol {

  protected val errorUsuarioNoTienePermiso = ErrorMessage("409.0", "Usuario no tiene permisos", "Usuario no tiene permisos").toJson
  protected val errorUsuarioExiste = ErrorMessage("409.1", "Usuario ya existe", "Usuario ya existe").toJson
  protected val errorClienteNoExiste = ErrorMessage("409.2", "No existe el cliente", "No existe el cliente").toJson
  protected val errorUsuarioCorreoExiste = ErrorMessage("409.3", "Correo ya existe", "Correo ya existe").toJson
  protected val errorClienteInactivo = ErrorMessage("409.4", "Cliente inactivo", "Cliente inactivo").toJson
  protected val errorContrasenaActualNoExiste = ErrorMessage("409.5", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  protected val errorIpExiste = ErrorMessage("409.6", "Direccion Ip ya existe", "Direccion Ip ya existe").toJson
  protected val errorTipoIdentificacion = ErrorMessage("409.7", "Tipo identificación no concuerda", "Tipo identificación no concuerda").toJson
  protected val errorUsuarioNoExiste = ErrorMessage("409.8", "No existe el Usuario", "No existe el Usuario").toJson
  protected val errorEstadoUsuarioClienteInd = ErrorMessage("409.9", "El estado actual del usuario no permite el reinicio de contrasena", "El estado actual del usuario no permite el reinicio de contrasena").toJson
  protected val errorInterno = ErrorMessage("500", "errorInterno", "errorInterno").toJson
  protected val errorCorreoNoExiste = ErrorMessage("409.10", "No hay correo registrado", "No hay correo registrado en la base de datos de Alianza").toJson
  protected val errorEnviandoCorreo = ErrorMessage("409.15", "Error Correo", "Error enviando correo").toJson
  protected val errorValidacionAutovalidacion = ErrorMessage("409.16", "Error Validacion Configuración Autovalidacion", "Error Validacion Configuración Autovalidacion").toJson
}
