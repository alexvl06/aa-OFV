package portal.transaccional.autenticacion.service.util.ws

import co.com.alianza.infrastructure.dto.security.GenericUsuario

import scala.util.control.NoStackTrace

sealed abstract class GenericValidacionAutorizacion() extends NoStackTrace

case class GenericNoAutorizado(codigo: String, msj: String) extends GenericValidacionAutorizacion
case class GenericAutorizado[E <: GenericUsuario](usuario: E) extends GenericValidacionAutorizacion
