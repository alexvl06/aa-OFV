package portal.transaccional.autenticacion.service.drivers.horarioEmpresa

import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ EmpresaDAO, HorarioEmpresaDAO }
import portal.transaccional.autenticacion.service.drivers.horarioEmpresa.{ DataAccessTranslator => horarioTranslator }
import portal.transaccional.autenticacion.service.web.horarioEmpresa.ResponseObtenerHorario

import scala.concurrent.{ ExecutionContext, Future }

case class HorarioEmpresaDriverRepository(empresaDAO: EmpresaDAO, horarioEmpresaDAO: HorarioEmpresaDAO)(implicit val ex: ExecutionContext) extends HorarioEmpresaRepository {

  def obtenerHorarioEmpresa(identifiacionUsuario: String): Future[Option[ResponseObtenerHorario]] = {
    for {
      empresa <- empresaDAO.getByNit(identifiacionUsuario)
      horarioEmpresaEntity <- horarioEmpresaDAO.obtenerHorarioEmpresa(empresa.get.id)
      horarioEmpresa <- Future { horarioTranslator.entityToDto(horarioEmpresaEntity.get) }
    } yield horarioEmpresa
  }

  def agregarHorarioEmpresa(): Future[String] = ???

  def esDiaFestivo(): Future[Boolean] = ???

  def validarHorario(idUsuarioRecurso: Option[String], tipoIdentificacion: Option[Int]): Future[String] = ???

}
