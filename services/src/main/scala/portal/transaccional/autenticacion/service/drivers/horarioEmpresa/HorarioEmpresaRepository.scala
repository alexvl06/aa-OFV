package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import scala.concurrent.Future

trait HorarioEmpresaRepository {

  def obtenerHorarioEmpresa(): Future[String]

  def agregarHorarioEmpresa(): Future[String]

  def esDiaFestivo(): Future[Boolean]

  def validarHorario(idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]): Future[String]

}
