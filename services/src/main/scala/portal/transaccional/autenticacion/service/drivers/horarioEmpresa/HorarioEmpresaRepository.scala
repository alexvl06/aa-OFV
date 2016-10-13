package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.HorarioEmpresa
import portal.transaccional.autenticacion.service.web.horarioEmpresa.ResponseObtenerHorario

import scala.concurrent.Future

trait HorarioEmpresaRepository {

  def obtener(identificacion: String): Future[Option[ResponseObtenerHorario]]

  def agregar(usuario: UsuarioAuth, diaHabil: Boolean, sabado: Boolean, horaInicio: String, horaFin: String): Future[Int]

  def esDiaFestivo(fecha: String): Future[Boolean]

  def validar(user: UsuarioAuth, idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]): Future[Boolean]

  def validarHorario(horarioEmpresa: Option[HorarioEmpresa]): Future[Boolean]

}
