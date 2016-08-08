package co.com.alianza.infrastructure.dto.security

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente

/**
 * Representa el usuario autenticado
 *
 * @param id El id del usuario autenticado
 */
case class UsuarioAuth(id: Int, tipoCliente: TiposCliente, identificacionUsuario: String, tipoIdentificacion: Int)

