package portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario
import portal.transaccional.autenticacion.service.web.permisoInmobiliario.{ ConsultarAgenteInmobiliarioListResponse, ConsultarAgenteInmobiliarioResponse }
import portal.transaccional.autenticacion.service.web.agenteInmobiliario.{ConsultarAgenteInmobiliarioListResponse, ConsultarAgenteInmobiliarioResponse}

import scala.concurrent.Future

/**
  * Define las operaciones del repositorio de agentes inmobiliarios
  */
trait UsuarioInmobiliarioRepository {

  /**
    * Crea un agente inmobiliario
    *
    * @param tipoIdentificacion Tipo de identificación de la empresa a agregar el agente
    * @param identificacion     Número de identificación de la empresa a agregar el agente
    * @param correo             Correo electrónico del agente
    * @param usuario            Nombre de usuario del agente
    * @param nombre             Nombre completo del agente - Opcional
    * @param cargo              Cargo del agente en la empresa - Opcional
    * @param descripcion        Descripción del agente - Opcional
    * @return Un futuro con el identificador del agente agregado.
    *         Si el agente ya existe, el identificador que devuleve corresponde a cero (0)
    */
  def createAgenteInmobiliario(tipoIdentificacion: Int, identificacion: String,
                               correo: String, usuario: String,
                               nombre: Option[String], cargo: Option[String], descripcion: Option[String]): Future[Int]

  /**
    * Obtiene un agente inmobiliario dado su usuario
    *
    * @param identificacion Identificación del agente
    * @param usuario        Nombre de usuario del agente
    * @return Un futuro con el agente a buscar embebido en un option
    */
  def getAgenteInmobiliario(identificacion: String,
                            usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]]

  /**
    * Obtiene la lista de agentes inmobiliarios de la empresa
    *
    * @param identificacion Identificación de la empresa
    * @param nombre         Filtro - Nombre del agente inmobiliario - Opcional
    * @param usuario        Filtro - Nombre de usuario del agente inmobiliario - Opcional
    * @param correo         Filtro - Correo del agente inmobiliario - Opcional
    * @param estado         Filtro - Estado del agente inmobiliario - Opcional
    * @param pagina         Paginación - Número de página - Opcional
    * @param itemsPorPagina Paginación - Número de agentes por página - Opcional
    * @return La lista de agentes inmobiliarios (entidad de servicio)
    */
  def getAgenteInmobiliarioList(identificacion: String, nombre: Option[String], usuario: Option[String],
                                correo: Option[String], estado: Option[Int],
                                pagina: Option[Int], itemsPorPagina: Option[Int]): Future[ConsultarAgenteInmobiliarioListResponse]

  /**
    * Actualiza la información de un agente inmobiliario
    *
    * @param identificacion Identificación de la empresa
    * @param usuario        Nombre de usuario a actualizar
    * @param correo         Correo actualizado del agente
    * @param nombre         Nombre actualizado del agente
    * @param cargo          Cargo actualizado del agente
    * @param descripcion    Descripción actualizada del agente
    * @return Un futuro con la cantidad de filas actualizadas (0 si falla, 1 si se actualiza correctamente)
    */
  def updateAgenteInmobiliario(identificacion: String, usuario: String,
                               correo: String, nombre: Option[String],
                               cargo: Option[String], descripcion: Option[String]): Future[Int]

  /**
    * Activa o desactiva a un agente inmobiliario
    *
    * @param identificacion Identificación de la empresa
    * @param usuario        Nombre de usuario del agente a actualizar
    * @return Un futuro con el estado del agente modificado
    */
  def activateOrDeactivateAgenteInmobiliario(identificacion: String, usuario: String): Future[Option[ConsultarAgenteInmobiliarioResponse]]

  /**
   * Obtiene un agente inmobiliario dado su id, y su contraseña
   *
   * @param idUsuario  Id en base de datos del agente
   * @param contrasena Contraseña actual del agente
   * @return Un futuro con el agente a buscar embebido en un option
   */
  def getContrasena(contrasena: String, idUsuario : Int): Future[UsuarioAgenteInmobiliario]

  /**
   * Actualiza un agente inmobiliario dado su id, y su nueva contraseña
   *
   * @param idUsuario  Id en base de datos del agente
   * @param contrasena Contraseña nueva del agente
   * @return Un futuro con un entero, si es 0 no actualizo, 2 actualizo el registro.
   */
  def updateContrasena(contrasena: String , idUsuario : Int ): Future[Int]

}
