package co.com.alianza.persistence.dto

/**
 * Created by dfbaratov on 4/08/16.
 */
case class UsuarioLdapDTO(
  usuario: String,
  identificacion: Option[String],
  esSAC: Boolean
)