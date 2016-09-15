package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UsuarioAgenteInmobiliario

import scala.concurrent.Future

/**
 * Created by alexandra on 2016
 */
trait UsuarioEmpresarialInmobDAOs {

  def create(agenteInmob: UsuarioAgenteInmobiliario): Future[Int]

  def updateStateById(idUsuario: Int, estado: Int): Future[Int]

  def getByIdentityAndUser(idEmpresa: String, usuario: String): Future[Option[UsuarioAgenteInmobiliario]]

}
