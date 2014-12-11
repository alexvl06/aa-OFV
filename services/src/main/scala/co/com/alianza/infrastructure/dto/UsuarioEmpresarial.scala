package co.com.alianza.infrastructure.dto

import java.util.Date

/**
 * Created by manuel on 10/12/14.
 */
case class UsuarioEmpresarial(
                    id: Int,
                    correo: String,
                    fechaCaducidad: Date,
                    identificacion: String,
                    tipoIdentificacion: Int,
                    usuario: String,
                    estado: Int,
                    contrasena: Option[String],
                    numeroIngresosErroneos:Int,
                    ipUltimoIngreso:Option[String],
                    fechaUltimoIngreso:Option[Date],
                    fechaUltimaPeticion: Option[Date]
                               )
