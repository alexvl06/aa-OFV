package portal.transaccional.autenticacion.service.drivers.sesion

import akka.actor.ActorRef
import co.com.alianza.domain.aggregates.autenticacion.SesionActorSupervisor.SesionUsuarioCreada
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.Empresa
import co.com.alianza.infrastructure.messages.{ BuscarSesion, CrearSesionUsuario, InvalidarSesion, ValidarSesion }

import scala.concurrent.Future

/**
 * Created by s4n on 2016
 */
trait SesionRepository {

  def crearSesion (token : String, inactividad : Int, empresa : Option[Empresa]): Future[SesionUsuarioCreada]

  def eliminarSesion (token : String): Future[Any]

  def validarSesion(token: String): Future[Boolean]

  def obtenerSesion(token: String): Future[ActorRef]

}
