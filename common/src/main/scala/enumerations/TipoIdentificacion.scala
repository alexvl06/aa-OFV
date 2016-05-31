package enumerations

/**
 * Created by josegarcia on 10/06/14.
 */
object TipoIdentificacion extends Enumeration {

  protected case class Val(identificador: Int, name: String) extends super.Val(nextId, name)

  val CEDULA_CUIDADANIA = Val(1, "CC")
  val CEDULA_EXTRANJERIA = Val(2, "CE")
  val NIT = Val(3, "NIT")
  val FID = Val(4, "F")
  val TI = Val(5, "T")
  val SOCIEDAD_EXTRANJERA = Val(6, "Sociedad Extranjera")
  val PASAPORTE = Val(7, "Pasaporte")
  val REGISTRO_CIVIL = Val(8, "Registro Civil")
  val NUIP = Val(9, "Nuip")
  val GRUPO = Val(10, "Grupo")
}
