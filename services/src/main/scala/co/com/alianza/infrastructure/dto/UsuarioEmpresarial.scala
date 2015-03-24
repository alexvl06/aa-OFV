package co.com.alianza.infrastructure.dto

import java.util.Date
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente
import TiposCliente.TiposCliente
import co.com.alianza.commons.enumerations.TiposCliente

/**
 * Created by manuel on 10/12/14.
 */

case class UsuarioEmpresarialEstado(
                               id: Int,
                               correo: String,
                               fechaCaducidad: Date,
                               identificacion: String,
                               tipoIdentificacion: Int,
                               usuario: String,
                               estado: estadoUsuario,
                               contrasena: Option[String],
                               numeroIngresosErroneos: Int,
                               ipUltimoIngreso: Option[String],
                               fechaUltimoIngreso: Option[Date],
                               tipoCliente: TiposCliente,
                               nombreUsuario:Option[String]
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
                               nombreUsuario:Option[String]
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


case class estadoUsuario(id: Int, detalle: String)
