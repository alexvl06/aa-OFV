package portal.transaccional.autenticacion.service.drivers.ip

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.persistence.entities.{ IpsEmpresa, IpsUsuario }
import portal.transaccional.autenticacion.service.drivers.cliente.ClienteRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAdmin.UsuarioEmpresarialAdminRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgente.UsuarioEmpresarialRepository
import portal.transaccional.autenticacion.service.drivers.usuarioIndividual.UsuarioRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.{ EmpresaAdminDAOs, IpEmpresaDAOs, IpUsuarioDAOs }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by s4n on 2016
 */
case class IpDriverRepository(usuarioRepo: UsuarioRepository, usuarioAgenteRepo: UsuarioEmpresarialRepository, empresaDAO: EmpresaAdminDAOs,
    ipEmpresaDAO: IpEmpresaDAOs, usuarioAdminRepo: UsuarioEmpresarialAdminRepository, clienteCoreRepo: ClienteRepository, ipDAO: IpUsuarioDAOs)(implicit val ex: ExecutionContext) extends IpRepository {

  /**
   * Flujo:
   * 1) Se busca el usuario por id si no se encuentra se devuelve CredencialesInvalidas
   * 2) Se busca el usuario en el core de alianza si no se encuentra se deuvleve ClienteNoExisteEnCore
   * 3) Se valida el estado del cliente en el core
   * 4) Se relaciona la ip con el id del usuario
   */
  def agregarIpHabitualUsuario(idUsuario: String, clientIp: String): Future[String] = {
    for {
      usuario <- usuarioRepo.getByIdentificacion(idUsuario.toString)
      cliente <- clienteCoreRepo.getCliente(idUsuario.toString)
      clienteValido <- clienteCoreRepo.validarEstado(cliente)
      relacionarIp <- asociarIpUsuario(usuario.id.get, clientIp)
    } yield relacionarIp
  }

  /**
   * Flujo:
   * 1) Se busca el usuario por id si no se encuentra se devuelve CredencialesInvalidas
   * 2) Se busca el usuario en el core de alianza si no se encuentra se devuelve ClienteNoExisteEnCore
   * 3) Se valida el estado del admin en el core
   * 4) Se busca la empresa a la cual esta asociada el admin
   * 4) Se relaciona la ip con el id del admin
   */
  def agregarIPHabitualUsuarioEmpresarialAdmin(idUsuario: Int, clientIp: String): Future[String] = {
    for {
      usuarioAdmin <- usuarioAdminRepo.getById(idUsuario)
      cliente <- clienteCoreRepo.getCliente(usuarioAdmin.identificacion)
      estadoCore <- clienteCoreRepo.validarEstado(cliente)
      idEmpresa <- obtenerEmpresaIdUsuarioAdmin(idUsuario)
      relacionarIp <- asociarIpEmpresa(idEmpresa, clientIp)
    } yield relacionarIp
  }

  /**
   * Asocia la ip al usuario
   * @param idUsuario id del usuario
   * @param ipPeticion ip a asociar
   * @return Future[Validation[ErrorAutenticacion, Boolean] ]
   * Success => True
   * ErrorAutenticacion => PersistenceException
   */
  private def asociarIpUsuario(idUsuario: Int, ipPeticion: String): Future[String] = {
    ipDAO.create(IpsUsuario(idUsuario, ipPeticion)) flatMap {
      case r: String => Future.successful("Se asocio el usuario")
      case _ => Future.failed(ValidacionException("401.3", "No se pudo asociar la ip"))
    }
  }

  private def obtenerEmpresaIdUsuarioAdmin(idUsuario: Int): Future[Int] = {
    //log.info("Obteniendo empresa por idUsuario empresarial Admin")
    empresaDAO.obtenerIdEmpresa(idUsuario)
  }

  private def asociarIpEmpresa(idEmpresa: Int, ipPeticion: String): Future[String] = {
    ipEmpresaDAO.create(IpsEmpresa(idEmpresa, ipPeticion))
  }

}
