package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario

import scala.concurrent.Future

/**
  * Define las operaciones de base de datos de los agentes inmobiliarios
  */
trait UsuarioAgenteInmobDAOs {

  /**
    * Verifica si un agente existe
    *
    * @param id      Identificador del agente
    * @param identificacion     Número de identificación del agente
    * @param usuario Nombre de usuario del agente
    * @return Un futuro con un booleano indicando si el agente existe o no
    */
  def exists(id: Int, identificacion: String, usuario: String): Future[Boolean]

  /**
    * Crea un agente inmobiliario
    *
    * @param usuarioInmob Agente inmobiliario a crear
    * @return Un futuro con el id del agente creado.
    */
  def create(usuarioInmob: UsuarioAgenteInmobiliario): Future[Int]
}
