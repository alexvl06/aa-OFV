package co.com.alianza.commons.enumerations

/**
 * Created by manuel on 8/01/15.
 */
object PermisosFideicomisosCoreAlianza extends Enumeration {

  case class PermisosFideicomisosCoreAlianza(codigo: Int, nombre: String) extends Val(codigo, nombre)

  val `SI` = PermisosFideicomisosCoreAlianza(1, "S")
  val `NO` = PermisosFideicomisosCoreAlianza(2, "N")

}
