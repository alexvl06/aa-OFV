package co.com.alianza.infrastructure.anticorruption.contrasenas

import co.com.alianza.app.MainActors
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.clientes.DataAccessTranslator
import co.com.alianza.infrastructure.dto.Cliente
import co.com.alianza.persistence.entities.ReglasContrasenas
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import co.com.alianza.persistence.repositories.ReglasContrasenasRepository
import co.com.alianza.persistence.repositories.core.ClienteRepository

import scalaz.Validation
import scala.concurrent.{Future, ExecutionContext}
import scalaz.{Failure => zFailure, Success => zSuccess}
import co.com.alianza.util.clave.Crypto

/**
 * Created by david on 16/06/14.
 */
object DataAccessAdapter {

  implicit val ec: ExecutionContext = MainActors.dataAccesEx
  val repo = new ReglasContrasenasRepository()

  def consultarReglasContrasenas( ): Future[Validation[PersistenceException, List[ReglasContrasenas]]] = {
    repo.obtenerReglas()
  }

  def actualizarReglasContrasenas( regla: ReglasContrasenas ): Future[Validation[PersistenceException, Int]] = {
    repo.actualizar(regla)
  }

  def obtenerRegla( llave : String ): Future[Validation[PersistenceException, Option[ReglasContrasenas]]] = {
    repo.obtenerRegla( llave )
  }

  def ActualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = {
    repo.actualizarContrasena(Crypto.hashSha512(pw_nuevo), idUsuario)
  }

  def ActualizarContrasenaYCaducidad(idUsuario: Int, pw_nuevo: String, caducidad: Long): Future[Validation[PersistenceException, Int]] = {
    repo.actualizarContrasenaYCaducidad(idUsuario, Crypto.hashSha512(pw_nuevo), caducidad)
  }

}
