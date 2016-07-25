package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.config.DBConfig
import co.com.alianza.persistence.entities.IpsEmpresaTable
import slick.lifted.TableQuery

/**
 * Created by alexandra on 25/07/16.
 */
class IpEmpresaDAO()(implicit dcConfig: DBConfig) extends TableQuery(new IpsEmpresaTable(_)) {
  import dcConfig.db._
  import dcConfig.profile.api._

}
