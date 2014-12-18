package co.com.alianza.domain.aggregates.usuarios

import co.com.alianza.exceptions.PersistenceException

sealed trait ErrorValidacion{
  def msg:String
}

case class ErrorDocumentoExiste(msg:String)   extends ErrorValidacion
case class ErrorCorreoExiste(msg:String)      extends ErrorValidacion
case class ErrorClienteNoExiste(msg:String)   extends ErrorValidacion
case class ErrorContrasenaNoExiste(msg:String) extends ErrorValidacion
case class ErrorClienteInactivo(msg:String)   extends ErrorValidacion
case class ErrorFormatoClave(msg:String)   extends ErrorValidacion
case class ErrorPersistence(msg:String, exception:PersistenceException)   extends ErrorValidacion
case class ErrorCaptcha(msg:String)   extends ErrorValidacion
case class ErrorPin(msg:String)   extends ErrorValidacion
case class ErrorEstadoUsuarioOlvidoContrasena(msg:String)   extends ErrorValidacion
case class ErrorAgenteEmpresarialNoExiste(msg:String) extends ErrorValidacion
