package portal.transaccional.autenticacion.service.drivers.ip

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsUsuario }
import portal.transaccional.autenticacion.service.drivers.sesion.SesionRepository
import portal.transaccional.autenticacion.service.web.ip.IpResponse
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ EmpresaAdminDAOs, IpEmpresaDAOs, IpUsuarioDAOs }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n on 2016
 */
case class IpDriverRepository(empresaDAO: EmpresaAdminDAOs, ipEmpresaDAO: IpEmpresaDAOs,
    ipDAO: IpUsuarioDAOs, sesionRepo: SesionRepository)(implicit val ex: ExecutionContext) extends IpRepository {

  def obtenerIps(usuario: UsuarioAuth): Future[Seq[IpResponse]] = {
    usuario.tipoCliente match {
      case TiposCliente.clienteAdministrador =>
        for {
          idEmpresa <- empresaDAO.obtenerIdEmpresa(usuario.id)
          ips <- ipEmpresaDAO.getById(idEmpresa)
        } yield ips.map(ip => IpResponse(ip.ip))
      case TiposCliente.clienteIndividual => ipDAO.getById(usuario.id).map(_.map(ip => IpResponse(ip.ip)))
      case _ => Future.failed(ValidacionException("401.3", "Tipo de usuario no permitido"))
    }
  }

  def agregarIp(usuario: UsuarioAuth, ip: String): Future[String] = {
    usuario.tipoCliente match {
      case TiposCliente.clienteAdministrador =>
        for {
          idEmpresa <- empresaDAO.obtenerIdEmpresa(usuario.id)
          relacionarIp <- ipEmpresaDAO.create(IpsEmpresa(idEmpresa, ip))
          _ <- sesionRepo.agregarIpEmpresa(idEmpresa, ip)
        } yield relacionarIp
      case TiposCliente.clienteIndividual => ipDAO.create(IpsUsuario(usuario.id, ip))
      case _ => Future.failed(ValidacionException("401.3", "Tipo de usuario no permitido"))
    }
  }

  def eliminarIp(usuario: UsuarioAuth, ip: String): Future[Int] = {
    usuario.tipoCliente match {
      case TiposCliente.clienteAdministrador =>
        for {
          idEmpresa <- empresaDAO.obtenerIdEmpresa(usuario.id)
          relacionarIp <- ipEmpresaDAO.delete(IpsEmpresa(idEmpresa, ip))
          _ <- sesionRepo.eliminarIpEmpresa(idEmpresa, ip)
        } yield relacionarIp
      case TiposCliente.clienteIndividual => ipDAO.delete(IpsUsuario(usuario.id, ip))
      case _ => Future.failed(ValidacionException("401.3", "Tipo de usuario no permitido"))
    }
  }

}
