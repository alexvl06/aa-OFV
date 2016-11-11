package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{ UsuarioEmpresarial, UsuarioEmpresarialTable }
/**
  * Created by hernando in 2016
  */
trait UsuarioEmpresarialDAOs {

  def updateUpdateDate(idUsuario: Int, fechaActual: Timestamp): Future[Int]

  def updatePasswordById(idUsuario: Int, contrasena: String): Future[Int]
}
