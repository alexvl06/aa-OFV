package co.com.alianza.domain.aggregates.autenticacion.errores

import co.com.alianza.infrastructure.messages.ErrorMessage
import co.com.alianza.util.json.MarshallableImplicits._
import co.com.alianza.exceptions.PersistenceException

/**
 * Created by manuel on 3/03/15.
 */
sealed trait ErrorAutorizacion {
  def msg: String
}

case class ErrorSesionNoEncontrada() extends ErrorAutorizacion {
  override def msg = ErrorMessage("403.9", "Error Cliente Alianza", "Cliente inactivo en core de alianza").toJson
}

case class ErrorSesionIpInvalida() extends ErrorAutorizacion {
  override def msg = ErrorMessage("403.10", "Error Cliente Alianza", "Cliente inactivo en core de alianza").toJson
}

case class ErrorPersistencia(msg: String, e: PersistenceException) extends ErrorAutorizacion