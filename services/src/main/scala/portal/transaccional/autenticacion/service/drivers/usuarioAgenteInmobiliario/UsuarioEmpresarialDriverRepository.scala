package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.persistence.entities.{ UsuarioEmpresarial, UsuarioEmpresarialTable }
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.{ UsuarioEmpresarialRepository, UsuarioEmpresarialRepositoryG }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UsuarioEmpresarialDAO

import scala.concurrent.ExecutionContext

/**
 * Created by alexandra on 2016
 */
case class UsuarioEmpresarialDriverRepository (usuarioDAO : UsuarioEmpresarialDAO)(implicit val ex : ExecutionContext)
  extends UsuarioEmpresarialRepositoryG[UsuarioEmpresarialTable,UsuarioEmpresarial](usuarioDAO)
    with UsuarioEmpresarialRepository[UsuarioEmpresarial]
