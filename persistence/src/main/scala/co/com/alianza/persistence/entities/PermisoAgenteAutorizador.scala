package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 16/02/15.
 */
case class PermisoAgenteAutorizador (idAgente: Int, tipoTransaccion: Int, idAutorizador: Int)
class PermisoAgenteAutorizadorTable (tag: Tag) extends Table[PermisoAgenteAutorizador](tag, "PERMISO_AGENTE_AUTORIZADOR") {

  def idAgente   = column[Int]("ID_AGENTE")
  def tipoTransaccion   = column[Int]("TIPO_TRANSACCION")
  def idAutorizador   = column[Int]("ID_AUTORIZADOR")

  def pk = primaryKey("PERMISO_AGENTE_AUTORIZADOR_PK", (idAgente, tipoTransaccion, idAutorizador))

  def * = (idAgente, tipoTransaccion, idAutorizador) <> (PermisoAgenteAutorizador.tupled, PermisoAgenteAutorizador.unapply)
  def ? = (idAgente.?, tipoTransaccion.?, idAutorizador.?).shaped.<>({r=>import r._; _1.map(_=> PermisoAgenteAutorizador.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
}

class PermisoAgenteAutorizadorAdminTable (tag: Tag) extends Table[PermisoAgenteAutorizador](tag, "PERMISO_AGENTE_AUTORIZADOR_ADMIN") {

  def idAgente   = column[Int]("ID_AGENTE")
  def tipoTransaccion   = column[Int]("TIPO_TRANSACCION")
  def idAutorizador   = column[Int]("ID_AUTORIZADOR")

  def pk = primaryKey("PERMISO_AGENTE_AUTORIZADOR_ADMIN_PK", (idAgente, tipoTransaccion, idAutorizador))

  def * = (idAgente, tipoTransaccion, idAutorizador) <> (PermisoAgenteAutorizador.tupled, PermisoAgenteAutorizador.unapply)
  def ? = (idAgente.?, tipoTransaccion.?, idAutorizador.?).shaped.<>({r=>import r._; _1.map(_=> PermisoAgenteAutorizador.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
}