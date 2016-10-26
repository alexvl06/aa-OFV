package co.com.alianza.infrastructure.dto

import java.util.Date

import co.com.alianza.commons.enumerations.TiposCliente.TiposCliente

/**
 * Created by manuel on 10/12/14.
 */

case class UsuarioEmpresarialEstado(
  id: Int,
  correo: String,
  identificacion: String,
  tipoIdentificacion: Int,
  usuario: String,
  cargo: String,
  descripcion: String,
  estado: estadoUsuario,
  tipoCliente: TiposCliente,
  nombreUsuario: Option[String]
)

case class UsuarioEmpresarial(
  id: Int,
  correo: String,
  fechaCaducidad: Date,
  identificacion: String,
  tipoIdentificacion: Int,
  usuario: String,
  estado: Int,
  contrasena: Option[String],
  numeroIngresosErroneos: Int,
  ipUltimoIngreso: Option[String],
  fechaUltimoIngreso: Option[Date],
  tipoCliente: TiposCliente,
  nombreUsuario: Option[String]
)

case class UsuarioEmpresarialAdmin(
  id: Int,
  correo: String,
  fechaCaducidad: Date,
  identificacion: String,
  tipoIdentificacion: Int,
  usuario: String,
  estado: Int,
  contrasena: Option[String],
  numeroIngresosErroneos: Int,
  ipUltimoIngreso: Option[String],
  fechaUltimoIngreso: Option[Date],
  tipoCliente: TiposCliente
)

case class UsuarioInmobiliario(
  identificacion: String,
  tipoIdentificacion: Int,
  usuario: String,
  tipoCliente: Int
)

case class UsuarioEmpresa(
  id: Int,
  identificacion: String,
  tipoIdentificacion: Int
)

case class estadoUsuario(id: Int, detalle: String)
