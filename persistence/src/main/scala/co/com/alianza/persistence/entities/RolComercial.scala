package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RolComercial( id: Option[ Int ], nombre: String )

  class RolComercialTable( tag: Tag ) extends Table[ RolComercial ]( tag, "ROL_COMERCIAL" ) {

    def id = column[ Option[ Int ] ]( "ID", O.PrimaryKey, O.AutoInc )
    def nombre = column[ String ]( "NOMBRE" )

    def * = ( id, nombre ) <> ( RolComercial.tupled, RolComercial.unapply )
  }

