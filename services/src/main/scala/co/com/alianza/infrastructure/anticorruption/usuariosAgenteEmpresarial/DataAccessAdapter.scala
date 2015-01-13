package co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial

import java.sql.Timestamp

import co.com.alianza.persistence.entities.{PinEmpresa => ePinEmpresa, _}
import co.com.alianza.persistence.repositories.UsuariosEmpresarialRepository
import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import enumerations.EstadosEmpresaEnum

import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure, Success, Validation}
import co.com.alianza.persistence.messages.empresa.GetAgentesEmpresarialesRequest
import co.com.alianza.infrastructure.dto.{UsuarioEmpresarial => dtoUsuario}
import co.com.alianza.persistence.repositories.empresa.UsuariosEmpresaRepository
import co.com.alianza.persistence.entities.IpsUsuario
import co.com.alianza.persistence.entities.Empresa
import co.com.alianza.persistence.messages.empresa.GetAgentesEmpresarialesRequest
import co.com.alianza.persistence.entities.UsuarioEmpresarialEmpresa
import co.com.alianza.persistence.entities.{UsuarioEmpresarial => eUsuario}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.persistence.entities.IpsUsuario
import co.com.alianza.persistence.entities.Empresa
import co.com.alianza.persistence.messages.empresa.GetAgentesEmpresarialesRequest
import co.com.alianza.persistence.entities.UsuarioEmpresarialEmpresa

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

  def crearAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial:eUsuario) : Future[Validation[PersistenceException, Int]] = {
    repo.insertarAgenteEmpresarial(nuevoUsuarioAgenteEmpresarial)
  }

  def crearIpAgenteEmpresarial(ipAgenteEmpresarial:IpsUsuario) : Future[Validation[PersistenceException, Int]] = {
    repo.insertarIpAgenteEmpresarial(ipAgenteEmpresarial)
  }

  def crearIpsAgenteEmpresarial(ipAgenteEmpresarial:Seq[IpsUsuario]) : Future[Validation[PersistenceException, Option[Int]]] = {
    repo.insertarIpsAgenteEmpresarial(ipAgenteEmpresarial)
  }

  def eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial: Int, usoPinEmpresa: Int): Future[Validation[PersistenceException, Int]] = {
    repo.eliminarPinEmpresaReiniciarAnteriores(idUsuarioAgenteEmpresarial, usoPinEmpresa)
  }

  def obtenerEmpresaPorNit(nit: String) : Future[Validation[PersistenceException, Option[Empresa]]] = {
    repo.obtenerEmpresaPorNit(nit)
  }

  def asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa : UsuarioEmpresarialEmpresa) = {
    repo.asociarAgenteEmpresarialConEmpresa(usuarioEmpresarialEmpresa)
  }

  def obtenerUsuariosBusqueda(message:GetAgentesEmpresarialesRequest): Future[Validation[PersistenceException, List[dtoUsuario]]] = {
    val repo = new UsuariosEmpresaRepository()
    repo.obtenerUsuariosBusqueda(message.correo, message.usuario, message.nombre, message.estado, message.idClienteAdmin) map {
      x => transformValidationList(x)
    }
  }

  private def transformValidationList(origin: Validation[PersistenceException, List[eUsuario]]): Validation[PersistenceException, List[dtoUsuario]] = {
    origin match {
      case zSuccess(response: List[eUsuario]) => zSuccess(DataAccessTranslator.translateUsuario(response))
      case zFailure(error)    =>  zFailure(error)
    }
  }

}
