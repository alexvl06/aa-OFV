package co.com.alianza.persistence.repositories

import java.sql.Timestamp
import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException
import scalaz.Validation
import co.com.alianza.persistence.entities._
import slick.lifted.TableQuery
import CustomDriver.simple._
import co.com.alianza.persistence.entities.Usuario

/**
 *
 * @author seven4n
 */
//TODO : Borrarlo ! , ya esta en el refactor By : Alexa
class UsuariosRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuarios = TableQuery[PerfilUsuarioTable]
  val pinusuarios = TableQuery[PinUsuarioTable]

  def obtenerUsuarioNumeroIdentificacion(numeroIdentificacion: String): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).result.headOption)
      resolveTry(resultTry, "Consulta usuario con identificador " + numeroIdentificacion)
  }

  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.token === token).result.headOption)
      resolveTry(resultTry, "Consulta usuario con token" + token)
  }

  def guardar(usuario: Usuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((usuarios returning usuarios.map(_.id.get)) += usuario)
      resolveTry(resultTry, "Crea usuario")
  }

  def actualizarEstadoUsuario(numeroIdentificacion: String, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.estado).update(estado))
      resolveTry(resultTry, "Actualizar estado del usuario ")
  }

  def cambiarPassword(idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.id === idUsuario).map(_.contrasena).update(Some(password)))
      resolveTry(resultTry, "Cambiar la contraseña del usuario ")
  }

  def asociarPerfiles(perfiles: List[PerfilUsuario]): Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>
      val resultTry = perfiles.map(perfil => session.database.run(perfilesUsuarios += perfil))
      resolveTry(Future.sequence(resultTry), "Actualizar usuario en token")
  }

  def guardarPinUsuario(pinUsuario: PinUsuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((pinusuarios returning pinusuarios.map(_.id.get)) += pinUsuario)
      resolveTry(resultTry, "Agregar pin usuario")
  }

}
