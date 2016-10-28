package co.com.alianza.infrastructure.dto.security

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente._


trait GenericUsuario {
  val id: Int
  val tipoCliente: TiposCliente
  val identificacion: String
  val tipoIdentificacion: Int
}


/**
 * Representa el usuario autenticado
 *
 * @param id El id del usuario autenticado
 */

case class UsuarioAuth(id: Int, tipoCliente: TiposCliente.TiposCliente, identificacion: String, tipoIdentificacion: Int) extends GenericUsuario

