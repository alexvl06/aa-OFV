package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import portal.transaccional.autenticacion.service.web.horarioEmpresa.ResponseObtenerHorario

import scala.concurrent.Future

trait HorarioEmpresaRepository {

  def obtenerHorarioEmpresa(id: String): Future[Option[ResponseObtenerHorario]]

  def agregarHorarioEmpresa(): Future[String]

  def esDiaFestivo(): Future[Boolean]

  def validarHorario(idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]): Future[String]

}
