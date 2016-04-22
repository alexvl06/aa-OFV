package co.com.alianza.infrastructure.dto.security

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente
import spray.http.{ HttpResponse, HttpEntity }
import spray.json._

/**
 * Representa el usuario autenticado
 *
 * @param id El id del usuario autenticado
 */
case class UsuarioAuth(id: Int, tipoCliente: TiposCliente, identificacionUsuario: String)