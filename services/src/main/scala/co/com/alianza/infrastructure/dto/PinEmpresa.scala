package co.com.alianza.infrastructure.dto

/**
 * Created by S4N on 22/12/14.
 */

import java.util.Date

case class PinEmpresa(id: Option[Int], idUsuarioEmpresarial: Int, token: String, fechaExpiracion: Date, tokenHash: String)
