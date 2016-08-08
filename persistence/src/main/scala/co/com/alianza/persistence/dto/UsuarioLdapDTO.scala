package co.com.alianza.persistence.dto

import org.joda.time.DateTime

/**
 * Created by dfbaratov on 4/08/16.
 */
case class UsuarioLdapDTO(
  usuario: String,
  identificacion: Option[String],
  nombreCompleto: Option[String],
  sn: Option[String],
  givenName: Option[String],
  memberOf: Option[String],
  nombre: String,
  idRole: Option[Int],
  last_login_ip: Option[String],
  last_login_date: Option[DateTime]
)