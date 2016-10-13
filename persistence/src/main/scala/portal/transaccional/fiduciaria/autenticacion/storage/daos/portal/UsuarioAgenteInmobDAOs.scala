package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario
import enumerations.EstadosUsuarioEnumInmobiliario.estadoUsuarioInmobiliario

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Define las operaciones de base de datos de los agentes inmobiliarios
 */
trait UsuarioAgenteInmobDAOs {

  /**
   * Crea un agente inmobiliario
   *
   * @param usuarioInmob Agente inmobiliario a crear
   * @return Un futuro con el id del agente creado.
   */
  def create(usuarioInmob: UsuarioAgenteInmobiliario): Future[Int]

  /**
   * Verifica si un agente existe
   *
   * @param id             Identificador del agente
   * @param identificacion Número de identificación del agente
   * @param usuario        Nombre de usuario del agente
   * @return Un futuro con un booleano indicando si el agente existe o no
   */
  def exists(id: Int, identificacion: String, usuario: String): Future[Boolean]

  /**
   * Obtiene un agente inmobiliario dado su identificador
   *
   * @param id Identificador de base de datos del agente
   * @return Un futuro con el agente a buscar embebido en un option
   */
  def get(id: Int): Future[Option[UsuarioAgenteInmobiliario]]

  /**
   * Obtiene un agente inmobiliario dado su usuario
   *
   * @param identificacion Identificación del agente
   * @param usuario        Nombre de usuario del agente
   * @return Un futuro con el agente a buscar embebido en un option
   */
  def get(identificacion: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]]

  /**
   * Obtiene la lista sin paginar y sin filtros de agentes inmobiliarios de la empresa
   *
   * @param identificacion Identificación de la empresa
   * @return Un futuro con todos los agentes inmobiliarios de la empresa
   */
  def getAll(identificacion: String): Future[Seq[UsuarioAgenteInmobiliario]]

  /**
   * Obtiene la lista paginada de agentes inmobiliarios de la empresa
   *
   * @param identificacion Identificación de la empresa
   * @param nombre         Filtro - Nombre del agente inmobiliario - Opcional
   * @param usuario        Filtro - Nombre de usuario del agente inmobiliario - Opcional
   * @param correo         Filtro - Correo del agente inmobiliario - Opcional
   * @param estado         Filtro - Estado del agente inmobiliario - Opcional
   * @param pagina         Paginación - Número de página - Opcional
   * @param itemsPorPagina Paginación - Número de agentes por página - Opcional
   * @return Una tupla de 5 elementos de la siguiente forma: <br/>
   *         <b>(pagina, maxItemsPorPagina, totalItemsEnPagina, totalItemsEnBD, itemsPagina)<b/>
   */
  def getAll(identificacion: String, nombre: Option[String],
    usuario: Option[String], correo: Option[String], estado: Option[Int], pagina: Option[Int],
    itemsPorPagina: Option[Int])(implicit ec: ExecutionContext): Future[(Int, Int, Int, Int, Seq[UsuarioAgenteInmobiliario])]

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
  def update(identificacion: String, usuario: String,
    correo: String, nombre: Option[String],
    cargo: Option[String], descripcion: Option[String]): Future[Int]

  /**
   * Actualiza el estado de un agente inmobiliario
   *
   * @param identificacion Identificación de la empresa
   * @param usuario        Nombre de usuario del agente a actualizar
   * @param estado         Estado del agente a actualizar
   * @return Un futuro con la cantidad de filas actualizadas (0 si falla, 1 si se actualiza correctamente)
   */
  def updateState(identificacion: String, usuario: String, estado: estadoUsuarioInmobiliario): Future[Int]

  /**
   * Obtiene un agente inmobiliario dado su id, y su contraseña
   *
   * @param idUsuario  Id en base de datos del agente
   * @param contrasena Contraseña actual del agente
   * @return Un futuro con el agente a buscar embebido en un option
   */
  def getContrasena(contrasena: String, idUsuario: Int): Future[Option[UsuarioAgenteInmobiliario]]

  /**
   * Actualiza un agente inmobiliario dado su id, y su nueva contraseña
   *
   * @param idUsuario  Id en base de datos del agente
   * @param contrasena Contraseña nueva del agente
   * @return Un futuro con un entero, si es 0 no actualizo, 2 actualizo el registro.
   */
  def updateContrasena(contrasena: String, idUsuario: Int): Future[Int]
}
