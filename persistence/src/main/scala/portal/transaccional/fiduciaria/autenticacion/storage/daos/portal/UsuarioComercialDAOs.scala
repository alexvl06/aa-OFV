package portal.transaccional.fiduciaria.autenticacion.storage.daos.portal

import co.com.alianza.persistence.entities.{User, UsuarioComercial}
import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * Created by dfbaratov on 4/08/16.
  */
trait UsuarioComercialDAOs {

  def createSchema(): Future[ Unit ]

  def dropSchema(): Future[ Unit ]

  def insertOrUpdate(newUser: User, ip: String )

  def insert( newUser: User, ip: String, date: DateTime ): Future[ Int ]

  def updateLastLoginValues( newUser: String, ip: String, date: DateTime ): Future[ Int ]

  def findByUsername( username: String ): Future[ Option[ UsuarioComercial ] ]

}
