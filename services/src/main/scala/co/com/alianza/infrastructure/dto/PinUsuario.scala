package co.com.alianza.infrastructure.dto

import java.util.Date

case class PinUsuario(id: Option[Int], idUsuario:Int, token: String, fechaExpiracion: Date, tokenHash: String)
