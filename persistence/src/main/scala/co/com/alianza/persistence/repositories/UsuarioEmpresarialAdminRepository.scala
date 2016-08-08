package co.com.alianza.persistence.repositories

import java.sql.Timestamp

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.persistence.entities.CustomDriver.simple._
import co.com.alianza.persistence.entities._
import enumerations.EstadosEmpresaEnum
import slick.lifted.TableQuery

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.Validation

class UsuarioEmpresarialAdminRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val UsuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val pinusuariosEmpresarialesAdmin = TableQuery[PinUsuarioEmpresarialAdminTable]
  val Empresas = TableQuery[EmpresaTable]

  //Ya esta en el refacto [AlianzaDAO.getAdminToken]
  def obtenerUsuarioToken(token: String): Future[Validation[PersistenceException, Option[(UsuarioEmpresarialAdmin, Int)]]] = loan {
    implicit session =>
      val query = for {
        (clienteAdministrador, empresa) <- usuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
          (uea, ueae) => uea.token === token && uea.id === ueae.idUsuarioEmpresarialAdmin
        } join Empresas on {
          case ((uea, ueae), e) => ueae.idEmpresa === e.id
        }
      } yield {
        (clienteAdministrador._1, empresa.estadoEmpresa)
      }

      val resultTry = session.database.run(query.result.headOption)
      resolveTry(resultTry, "Consulta cliente administrador por token: " + token)
  }

  //Ya esta [UsuarioEmpresarialAdminDAO.deleteToken]
  def invalidarTokenUsuario(token: String): Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.token === token).map(_.token).update(Some(null)))
      resolveTry(resultTry, "Invalidar token usuario")
  }

  //Ya esta [UsuarioEmpresarialAdminDAO.createToken]
  def asociarTokenUsuario(usuarioId: Int, token: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === usuarioId).map(_.token).update(Some(token)))
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

  def actualizarEstadoUsuario(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).map(_.estado).update(estado))
      resolveTry(resultTry, "Actualizar estado de usuario cliente admin")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.updatePassword]
  def cambiarPassword(idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {
        u <- usuariosEmpresarialesAdmin.filter(_.id === idUsuario)
      } yield (u.contrasena, u.numeroIngresosErroneos)
      val resultTry = session.database.run(query.update((Some(password), 0)))
      resolveTry(resultTry, "Cambiar la contraseña de usuario cliente admin")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.updateIncorrectEntries]
  def actualizarNumeroIngresosErroneos(idUsuario: Int, numeroIntentos: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).map(_.numeroIngresosErroneos).update(numeroIntentos))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en numeroIngresosErroneos ")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.updateStateById]
  def actualizarIpUltimoIngreso(idUsuario: Int, ipActual: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).map(_.ipUltimoIngreso).update(Some(ipActual)))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en ipUltimoIngreso ")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.updateLastDate]
  def actualizarFechaUltimoIngreso(idUsuario: Int, fechaActual: Timestamp): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).map(_.fechaUltimoIngreso).update(Some(fechaActual)))
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.getById]
  def obtenerUsuarioEmpresarialAdminPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.id === idUsuario).result.headOption)
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  //ya esta en [AlianzaDAO.createPinAdmin]
  def guardarPinUsuarioClienteAdmin(pinUsuario: PinUsuarioEmpresarialAdmin): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = session.database.run((pinusuariosEmpresarialesAdmin returning pinusuariosEmpresarialesAdmin.map(_.id.get)) += pinUsuario)
      resolveTry(resultTry, "Agregar pin usuario empresarial administrador")
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.getByToken]
  def obtenerUsuarioPorToken(token: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = session.database.run(usuariosEmpresarialesAdmin.filter(_.token === token).result.headOption)
      resolveTry(resultTry, "Consulta usuario empresarial con token" + token)
  }

  // ya esta en [UsuarioEmpresarialAdminDAO.getByNit]
  def existeUsuarioEmpresarialAdminActivo(nitEmpresa: String): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val estado = EstadosEmpresaEnum.activo.id
      val query = usuariosEmpresarialesAdmin.filter(u => u.identificacion === nitEmpresa && u.estado === estado).exists.result
      val resultTry = session.database.run(query)

      resolveTry(resultTry, "Validacion: existe usuario empresarial admin activo")
  }

}