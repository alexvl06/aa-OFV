package co.com.alianza.infrastructure.messages

import co.com.alianza.infrastructure.dto.Empresa

case class ActualizarSesion()

case class CrearSesionUsuario(token: String, tiempoExpiracion: Int, empresa: Option[Empresa] = None)

case class InvalidarSesion(token: String)

case class ExpirarSesion()

case class BuscarSesion(token: String)

case class ObtenerEmpresaSesionActorId(empresaId: Int)

case class ValidarSesion(token: String)

case class OptenerEmpresaActorPorId(empresaId: Int)

