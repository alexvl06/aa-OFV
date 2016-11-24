package portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario

import co.com.alianza.exceptions.ValidacionAutorizacion
import co.com.alianza.infrastructure.dto.UsuarioInmobiliarioAuth
import co.com.alianza.persistence.entities.RecursoBackendInmobiliario
import portal.transaccional.autenticacion.service.util.ws.GenericValidacionAutorizacion

import scala.concurrent.Future

/**
 * Created by alexandra in 2016
 */
trait AutorizacionRepository {

  def autorizar(token: String, encriptedToken: String, url: Option[String], ip: String, tipoCliente : String): Future[GenericValidacionAutorizacion]

  def filtrarRecuros(agente: UsuarioInmobiliarioAuth, recursos: Seq[RecursoBackendInmobiliario], urlO: Option[String]): Future[GenericValidacionAutorizacion]

}
