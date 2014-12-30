package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{PinEmpresa => ePinEmpresa, UsuarioEmpresarialEmpresa, Empresa, IpsUsuario, UsuarioEmpresarial}
import co.com.alianza.persistence.repositories.UsuariosEmpresarialRepository
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import enumerations.EstadosEmpresaEnum

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation

/**
 * Created by S4N on 16/12/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  val repo = new UsuariosEmpresarialRepository()

  def validacionAgenteEmpresarial( numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, Option[(Int,Int)]]] = {
    repo.validacionAgenteEmpresarial(numIdentificacionAgenteEmpresarial: String, correoUsuarioAgenteEmpresarial: String, tipoIdentiAgenteEmpresarial: Int, idClienteAdmin: Int)
  }

  def CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial: Int, estado : EstadosEmpresaEnum.estadoEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.CambiarEstadoAgenteEmpresarial(idUsuarioAgenteEmpresarial, estado)
  }

  def crearPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial: ePinEmpresa): Future[Validation[PersistenceException, Int]] = {
    repo.guardarPinEmpresaAgenteEmpresarial(pinEmpresaAgenteEmpresarial)
  }

  def crearAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial:UsuarioEmpresarial) : Future[Validation[PersistenceException, Int]] = {
    repo.insertarAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial)
  }

  def crearIpAgenteEmpresarial(ipAgenteEmpresarial:IpsUsuario) : Future[Validation[PersistenceException, Int]] = {
    repo.insertarIpAgenteEmpresarial(ipAgenteEmpresarial)
  }

  def crearIpsAgenteEmpresarial(ipAgenteEmpresarial:Seq[IpsUsuario]) : Future[Validation[PersistenceException, Option[Int]]] = {
    repo.insertarIpsAgenteEmpresarial(ipAgenteEmpresarial)
  }

  def obtenerEmpresaPorNit(nit: String) : Future[Validation[PersistenceException, Option[Empresa]]] = {
    repo.obtenerEmpresaPorNit(nit)
  }

  def asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa : UsuarioEmpresarialEmpresa) = {
    repo.asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa)
  }

}
