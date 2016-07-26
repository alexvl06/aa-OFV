package portal.transaccional.autenticacion.service.drivers.empresa

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.Empresa
import enumerations.empresa.EstadosDeEmpresaEnum
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.EmpresaDAOs

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by hernando on 26/07/16.
 */
case class EmpresaDriverRepository(empresaDAO: EmpresaDAOs)(implicit val ex: ExecutionContext) extends EmpresaRepository {

  def getByIdentity(nit: String): Future[Option[Empresa]] = {
    empresaDAO.getByIdentity(nit)
  }

  def validarEmpresa(empresaOption: Option[Empresa]): Future[Boolean] = {
    empresaOption match {
      case Some(empresa: Empresa) =>
        val estadoActiva: Int = EstadosDeEmpresaEnum.activa.id
        empresa.estadoEmpresa match {
          case `estadoActiva` => Future.successful(true)
          case _ => Future.failed(ValidacionException("401.15", "Empresa Acceso Denegado"))
        }
      case _ => Future.failed(ValidacionException("401.2", "Error Cliente Alianza"))
    }
  }

}
