package co.com.alianza.persistence.entities

import CustomDriver.simple._

/**
 * Created by manuel on 9/01/15.
 */
case class PermisoTransaccionalUsuarioEmpresarialAutorizador (idEncargo: String, idAgente: Int, tipoTransaccion: Int, idAutorizador: Int)
class PermisoTransaccionalUsuarioEmpresarialAutorizadorTable (tag: Tag) extends Table[PermisoTransaccionalUsuarioEmpresarialAutorizador](tag, "PERMISO_TX_USUARIO_EMPRESARIAL_AUTORIZADOR") {

  def idEncargo  = column[String]("ID_ENCARGO")
  def idAgente   = column[Int]("ID_USUARIO_EMPRESARIAL")
  def tipoTransaccion   = column[Int]("TIPO_TRANSACCION")
  def idAutorizador   = column[Int]("ID_AUTORIZADOR")

  def pk = primaryKey("PERMISO_TX_USUARIO_EMPRESARIAL_AUTORIZADOR_PK", (idEncargo, idAgente, tipoTransaccion, idAutorizador))

  def * = (idEncargo, idAgente, tipoTransaccion, idAutorizador) <> (PermisoTransaccionalUsuarioEmpresarialAutorizador.tupled, PermisoTransaccionalUsuarioEmpresarialAutorizador.unapply)
  def ? = (idEncargo.?, idAgente.?, tipoTransaccion.?, idAutorizador.?).shaped.<>({r=>import r._; _1.map(_=> PermisoTransaccionalUsuarioEmpresarialAutorizador.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))
}