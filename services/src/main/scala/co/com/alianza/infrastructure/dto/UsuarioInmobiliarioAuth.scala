package co.com.alianza.infrastructure.dto

import co.com.alianza.commons.enumerations.TiposCliente._
import co.com.alianza.infrastructure.dto.security.GenericUsuario

case class UsuarioInmobiliarioAuth(
  id: Int,
  tipoCliente: TiposCliente,
  identificacion: String,
  tipoIdentificacion: Int,
  usuario: String
) extends GenericUsuario