package co.com.alianza.commons.enumerations

/**
 * Created by manuel on 8/01/15.
 */
object TiposPermisosTransaccionales extends Enumeration {

  case class TipoPermisoTransaccional(codigo: Int, nombre: String) extends Val(codigo, nombre)

  val `POR MONTO` = TipoPermisoTransaccional(1, "Por monto")
  val `POR PERSONA` = TipoPermisoTransaccional(2, "Por persona")
  val `POR MONTO Y PERSONA` = TipoPermisoTransaccional(3, "Por monto y persona")

}
