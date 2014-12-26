package co.com.alianza.infrastructure.dto

import java.util.Date
import co.com.alianza.commons.enumerations.TiposCliente
import TiposCliente._
import co.com.alianza.commons.enumerations.TiposCliente

/**
 *
 * @author seven4n
 */
case class Usuario(
                    id: Option[Int],
                    correo: String,
                    fechaCaducidad: Date,
                    identificacion: String,
                    tipoIdentificacion: Int,
                    estado: Int,
                    contrasena: Option[String],
                    numeroIngresosErroneos: Int,
                    ipUltimoIngreso: Option[String],
                    fechaUltimoIngreso: Option[Date],
                    tipoCliente: TiposCliente
                    )


