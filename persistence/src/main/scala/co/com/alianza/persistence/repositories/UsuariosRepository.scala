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
class UsuariosRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val usuarios = TableQuery[UsuarioTable]
  val perfilesUsuarios = TableQuery[PerfilUsuarioTable]
  val pinusuarios = TableQuery[PinUsuarioTable]

  def obtenerUsuarios(): Future[Validation[PersistenceException, Seq[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.result)
      resolveTry(resultTry, "Consulta todos los Usuarios")
  }

  def obtenerUsuarioNumeroIdentificacion(numeroIdentificacion: String): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).result.headOption)
      resolveTry(resultTry, "Consulta usuario con identificador " + numeroIdentificacion)
  }

  def obtenerUsuarioId(idUsuario: Int): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Consulta usuario con identificador " + idUsuario)
  }

  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.token === token).result.headOption)
      resolveTry(resultTry, "Consulta usuario con token" + token)
  }

  def obtenerUsuarioCorreo(correo: String): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.correo === correo).result.headOption)
      resolveTry(resultTry, "Consulta usuario con correo " + correo)
  }

  def consultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[Usuario]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(x => x.contrasena === pw_actual && x.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Consulta contrasena actual de usuario " + pw_actual)
  }

  def guardar(usuario: Usuario): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((usuarios returning usuarios.map(_.id.get)) += usuario)
      resolveTry(resultTry, "Crea usuario")
  }

  def asociarTokenUsuario(numeroIdentificacion: String, token: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.token).update(Some(token)))
      resolveTry(resultTry, "Actualizar usuario en token")
  }

  def invalidarTokenUsuario(token: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.token === token).map(_.token).update(Some(null)))
      resolveTry(resultTry, "Invalidar token usuario")
  }

  def actualizarNumeroIngresosErroneos(numeroIdentificacion: String, numeroIntentos: Int): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.numeroIngresosErroneos).update(numeroIntentos))
      resolveTry(resultTry, "Actualizar usuario en numeroIngresosErroneos ")
  }

  def actualizarNumeroIngresosErroneos(idUsuario: Int, numeroIntentos: Int): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
      resolveTry(resultTry, "Actualizar usuario en numeroIngresosErroneos ")
  }

  def actualizarIpUltimoIngreso(numeroIdentificacion: String, ipActual: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.ipUltimoIngreso).update(Some(ipActual)))
      resolveTry(resultTry, "Actualizar usuario en actualizarIpUltimoIngreso ")
  }

  def actualizarFechaUltimoIngreso(numeroIdentificacion: String, fechaActual: Timestamp): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
      resolveTry(resultTry, "Actualizar usuario en actualizarFechaUltimoIngreso ")
  }

  def actualizarEstadoUsuario(numeroIdentificacion: String, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.identificacion === numeroIdentificacion).map(_.estado).update(estado))
      resolveTry(resultTry, "Actualizar estado del usuario ")
  }

  def actualizarEstadoUsuario(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuarios.filter(_.id === idUsuario).map(_.estado).update(estado))
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
