package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by alexandra on 2016
 */
case class PermisoAgenteInmobiliario(idAgente: Int, proyecto: Int, fideicomiso: Int, tipoPermiso: Int)

class PermisoInmobiliarioTable(tag: Tag) extends Table[PermisoAgenteInmobiliario](tag, "PERMISO_INMOBILIARIO") {

  def idAgente = column[Int]("ID_AGENTE_INMOBILIARIO")
  def proyecto = column[Int]("ID_PROYECTO")
  def fideicomiso = column[Int]("ID_FIDEICOMISO")
  def tipoPermiso = column[Int]("ID_PERMISO")

  def pk = primaryKey("PERMISO_AGENTE_PK", (idAgente, proyecto, fideicomiso, tipoPermiso))

  def * = (idAgente, proyecto, fideicomiso, tipoPermiso) <> (PermisoAgenteInmobiliario.tupled, PermisoAgenteInmobiliario.unapply)
}
