package co.com.alianza.domain.aggregates.empresa

import co.com.alianza.exceptions.PersistenceException

/**
 * Created by S4N on 22/12/14.
 */

sealed trait ErrorValidacionEmpresa {
  def msg:String
}

case class ErrorPersistenceEmpresa(msg:String, exception:PersistenceException) extends ErrorValidacionEmpresa
case class ErrorEstadoAgenteEmpresarial(msg:String) extends ErrorValidacionEmpresa
case class ErrorAgenteEmpNoExiste(msg:String) extends ErrorValidacionEmpresa