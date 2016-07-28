package co.com.alianza.infrastructure.dto

import java.util.Date
import co.com.alianza.commons.enumerations.TiposCliente
import TiposCliente._

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
  numeroIngresosErroneos: Int,
  ipUltimoIngreso: Option[String],
  fechaUltimoIngreso: Option[Date],
  tipoCliente: TiposCliente
)

