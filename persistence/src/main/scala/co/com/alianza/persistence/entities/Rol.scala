package co.com.alianza.persistence.entities

import co.com.alianza.persistence.config.DBConfig


case class Rol( id: Option[ Int ], nombre: String )

trait RolesTable { this: DBConfig =>
  import profile.api._
  class Roles( tag: Tag ) extends Table[ Rol ]( tag, "roles" ) {

    def id = column[ Option[ Int ] ]( "id", O.PrimaryKey, O.AutoInc )
    def nombre = column[ String ]( "nombre" )
    def * = ( id, nombre ) <> ( ( Rol.apply _ ).tupled, Rol.unapply )
  }

  /**
   * Table query reference
   */
  val roles: TableQuery[ Roles ] = TableQuery[ Roles ]

}
