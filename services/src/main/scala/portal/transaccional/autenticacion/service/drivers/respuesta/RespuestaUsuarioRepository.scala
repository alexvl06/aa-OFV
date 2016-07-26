package portal.transaccional.autenticacion.service.drivers.respuesta

import co.com.alianza.persistence.entities.RespuestasAutovalidacionUsuario

import scala.concurrent.Future

/**
 * Created by hernando on 25/07/16.
 */
trait RespuestaUsuarioRepository {

  def getRespuestasById(idUsuario: Int): Future[Seq[RespuestasAutovalidacionUsuario]]

  def validarRespuestas(respuestas: Seq[RespuestasAutovalidacionUsuario]): Future[Boolean]

}
