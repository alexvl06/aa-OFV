package co.com.alianza.infrastructure.dto

import java.util.Date

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
                    numeroIngresosErroneos:Int,
                    ipUltimoIngreso:Option[String],
                    fechaUltimoIngreso:Option[Date]
                  )


