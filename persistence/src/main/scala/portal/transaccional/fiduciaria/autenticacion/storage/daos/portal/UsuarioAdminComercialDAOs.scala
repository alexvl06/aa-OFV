package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.UsuarioAdminComercial
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Created by dfbaratov on 4/08/16.
  */
trait UsuarioAdminComercialDAOs {

  def insert( admin: UsuarioAdminComercial ): Future[ Option[ Int ] ]

  def updatePassword( username: String, newPassword: String ): Future[ Int ]

  def findByUsername( username: String ): Future[ Option[ UsuarioAdminComercial ] ]

  def updateLastLoginFields( username: String, ip: String, date: DateTime ): Future[ Int ]

  def findAndUpdateByRecoverInfo( info: String, password: String, date: DateTime ): Future[ Int ]

  def findByRecoverInfo( info: String ): Future[ Option[ UsuarioAdminComercial ] ]
}
