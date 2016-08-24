package co.com.alianza.persistence.entities

import CustomDriver.simple._

case class RolRecursoComercial(  idRol: Option[ Int ],  idRecurso: Option[ Int ] )

  class RolRecursoComercialTable( tag: Tag ) extends Table[RolRecursoComercial]( tag, "ROL_RECURSO_COMERCIAL" ) {


    def idRol = column[ Option[ Int ] ]( "ID_ROL" , O.PrimaryKey )
    def idRecurso = column[ Option[ Int ] ]( "ID_RECURSO" , O.PrimaryKey)


    //def rolesFK = foreignKey( "fk_rol", idRol, roles )( _.id )
    //def recursosFK = foreignKey( "fx_recurso", idRecurso, recursos )( _.id )

    //    def * = ( idRol, idRecurso ) <> ( ( RolRecurso.apply _ ).tupled, RolRecurso.unapply )
    def * = ( idRol, idRecurso ) <> (  RolRecursoComercial.tupled, RolRecursoComercial.unapply )
  }


