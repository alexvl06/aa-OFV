package enumerations

import com.fasterxml.jackson.core
import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import com.fasterxml.jackson.core.`type`.TypeReference

/**
 * Created by s4n on 10/06/14.
 */
object TipoIdentificacionComercial extends Enumeration {

  case class Val(identificador: Int, nombre: String, codFiduciaria: Option[String], codValores: Option[String])
    extends super.Val(nextId, nombre) with Serializable

  val CARNET_DIPLOMATICO = Val(0, "CARNET DIPLOMATICO", Some("D"), None)
  val CEDULA_CUIDADANIA = Val(1, "CEDULA DE CIUDADANIA", Some("C"), Some("C"))
  val CEDULA_EXTRANJERIA = Val(2, "CEDULA DE EXTRANJERIA", Some("E"), Some("E"))
  val CERTIFICADO_CONSTITUCION = Val(3, "CERTIFICADO DE CONSTITUCION", Some("J"), None)
  val FIDEICOMISO = Val(4, "FIDEICOMISO", Some("F"), None)
  val NIT = Val(5, "NIT", Some("A"), Some("N"))
  val NIT_PERSONA_NATURAL = Val(6, "NIT PERSONA NATURAL", Some("N"), None)
  val NUIP = Val(7, "NUIP", None, Some("I"))
  val NUMERO_VISA = Val(8, "NUMERO VISA", Some("V"), None)
  val OTRO = Val(9, "OTRO", Some("O"), None)
  val PASAPORTE = Val(10, "PASAPORTE", Some("P"), Some("P"))
  val REGISTRO_CIVIL = Val(11, "REGISTRO CIVIL", Some("R"), Some("G"))
  val REGISTRO_PUBLICO = Val(12, "REGISTRO PUBLICO", Some("G"), None)
  val REGISTRO_PUBLICO_CONSTITUCION = Val(13, "REGISTRO PUBLICO DE CONSTITUCION", Some("U"), Some("X"))
  val SOCIEDAD_EXTRANJERA = Val(14, "SOCIEDAD EXTRANJERA", Some("X"), Some("O"))
  val TARJETA_IDENTIDAD = Val(15, "TARJETA DE IDENTIDAD", Some("T"), Some("T"))

  def obtenerTodos(): List[TipoIdentificacionComercialDTO] = {
    List(CARNET_DIPLOMATICO, CEDULA_CUIDADANIA, CEDULA_EXTRANJERIA, CERTIFICADO_CONSTITUCION, FIDEICOMISO, NIT,
      NIT_PERSONA_NATURAL, NUIP, NUMERO_VISA, OTRO, PASAPORTE, REGISTRO_CIVIL, REGISTRO_PUBLICO,
      REGISTRO_PUBLICO_CONSTITUCION, SOCIEDAD_EXTRANJERA, TARJETA_IDENTIDAD).map(enumToDto(_))
  }

  private def enumToDto(tipo: TipoIdentificacionComercial.Val): TipoIdentificacionComercialDTO = {
    TipoIdentificacionComercialDTO(tipo.identificador, tipo.nombre, tipo.codFiduciaria, tipo.codValores)
  }

}

case class TipoIdentificacionComercialDTO(identificador: Int, nombre: String, codFiduciaria: Option[String], codValores: Option[String])
