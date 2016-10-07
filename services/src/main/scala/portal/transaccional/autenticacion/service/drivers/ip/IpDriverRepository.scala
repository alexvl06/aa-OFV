package portal.transaccional.autenticacion.service.drivers.ip

import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsUsuario }
import portal.transaccional.autenticacion.service.web.ip.IpResponse
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ EmpresaAdminDAOs, IpEmpresaDAOs, IpUsuarioDAOs }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n on 2016
 */
case class IpDriverRepository(empresaDAO: EmpresaAdminDAOs, ipEmpresaDAO: IpEmpresaDAOs, ipDAO: IpUsuarioDAOs)(implicit val ex: ExecutionContext) extends IpRepository {

  def obtenerIps(usuario: UsuarioAuth): Future[Seq[IpResponse]] = {
    usuario.tipoCliente match {
      case TiposCliente.clienteAdministrador =>
        for {
          idEmpresa <- empresaDAO.obtenerIdEmpresa(usuario.id)
          ips <- ipEmpresaDAO.getById(idEmpresa)
        } yield ips.map(ip => IpResponse(ip.ip))
      case TiposCliente.clienteIndividual => ipDAO.getAll().map(_.map(ip => IpResponse(ip.ip)))
      case _ => Future.failed(ValidacionException("401.3", "Tipo de usuario no permitido"))
    }
  }

  def agregarIp(usuario: UsuarioAuth, ip: String): Future[String] = {
    usuario.tipoCliente match {
      case TiposCliente.clienteAdministrador =>
        for {
          idEmpresa <- empresaDAO.obtenerIdEmpresa(usuario.id)
          relacionarIp <- ipEmpresaDAO.create(IpsEmpresa(idEmpresa, ip))
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
        } yield relacionarIp
      case TiposCliente.clienteIndividual => ipDAO.delete(IpsUsuario(usuario.id, ip))
      case _ => Future.failed(ValidacionException("401.3", "Tipo de usuario no permitido"))
    }
  }

  /*
  *
  *  private def agregarIpSesionEmpresa(empresaId: Int, ip: String) =
    sessionActor ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! AgregarIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }

  private def removerIpSesionEmpresa(empresaId: Int, ip: String) =
    sessionActor ? ObtenerEmpresaSesionActorId(empresaId) map {
      case Some(empresaSesionActor: ActorRef) =>
        empresaSesionActor ! RemoverIp(ip); zSuccess((): Unit)
      case None => zSuccess((): Unit)
      case _ => zFailure(PersistenceException(new Exception(), BusinessLevel, "Error"))
    }
  * */

}
