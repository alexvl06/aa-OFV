package enumerations

import com.fasterxml.jackson.core
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

/**
 * Created by josegarcia on 10/06/14.
 */
object TipoIdentificacion extends Enumeration {

   case class Val(identificador: Int, name: String, descripcion: String) extends super.Val(nextId, name) with Serializable

  val CEDULA_CUIDADANIA = Val(1, "CC", "Cédula de Ciudadanía")
  val CEDULA_EXTRANJERIA = Val(2, "CE", "Cédula de Extranjería")
  val NIT = Val(3, "NIT", "NIT")
  val FID = Val(4, "F", "FID")
  val TI = Val(5, "T", "Tarjeta de Identidad")
  val SOCIEDAD_EXTRANJERA = Val(6, "Sociedad Extranjera", "Sociedad Extranjera")
  val PASAPORTE = Val(7, "Pasaporte", "Pasaporte")
  val REGISTRO_CIVIL = Val(8, "Registro Civil", "Registro Civil")
  val NUIP = Val(9, "Nuip", "NUIP")
  val GRUPO = Val(10, "Grupo", "GRUPO")


  def obtenerTodos(): List[TipoIdentificacionDTO] = {
    List(CEDULA_CUIDADANIA, CEDULA_EXTRANJERIA, TI, PASAPORTE, REGISTRO_CIVIL, NUIP).map(enumToDto(_))
  }

  def obtenerTiposIdentificacionNatural(): List[TipoIdentificacionDTO] = {
     List(CEDULA_CUIDADANIA, CEDULA_EXTRANJERIA).map(enumToDto(_))
  }

  def obtenerTiposIdentificacionEmpresas(): List[TipoIdentificacionDTO] = {
    List(NIT, FID, SOCIEDAD_EXTRANJERA, GRUPO).map(enumToDto(_))
  }

  private def enumToDto(tipo: TipoIdentificacion.Val): TipoIdentificacionDTO = {
    TipoIdentificacionDTO(tipo.identificador, tipo.descripcion)
  }

}

case class TipoIdentificacionDTO(identificador: Int, descripcion: String)
