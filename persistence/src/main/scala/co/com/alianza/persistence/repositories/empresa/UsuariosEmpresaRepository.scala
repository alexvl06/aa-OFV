package co.com.alianza.persistence.repositories.empresa

import java.sql.Timestamp
import java.util.Calendar

import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.exceptions.PersistenceException

import scala.util.Try
import scalaz.Validation

import co.com.alianza.persistence.entities._

import scala.slick.lifted.TableQuery
import CustomDriver.simple._
import co.com.alianza.persistence.entities.Usuario
import scala.Some
import co.com.alianza.persistence.repositories.AlianzaRepository
import co.com.alianza.persistence.entities._
import scala.collection.mutable.ListBuffer

/**
 *
 * @author seven4n
 */
class UsuariosEmpresaRepository(implicit executionContext: ExecutionContext) extends AlianzaRepository {

  val empresas = TableQuery[EmpresaTable]
  val usuariosEmpresariales = TableQuery[UsuarioEmpresarialTable]
  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val usuariosEmpresarialesEmpresa = TableQuery[UsuarioEmpresarialEmpresaTable]
  val usuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val perfilesAgentes = TableQuery[PerfilAgenteAgenteTable]

  def obtenerUsuariosBusqueda(correoUsuario: String, usuario: String, nombreUsuario: String, estadoUsuario: Int, idClienteAdmin: Int): Future[Validation[PersistenceException, List[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = Try {
        var usuariosQuery = (for {
          (clienteAdministrador, agenteEmpresarial) <- usuariosEmpresarialesAdmin join usuariosEmpresarialesAdminEmpresa on {
            (uea, ueae) => uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin
          } join usuariosEmpresarialesEmpresa on {
            case ((uea, ueae), uee) => ueae.idEmpresa === uee.idEmpresa
          } join usuariosEmpresariales on {
            case (((uea, ueae), uee), ae) =>
              uee.idUsuarioEmpresarial === ae.id && ueae.idEmpresa === uee.idEmpresa && uea.id === ueae.idUsuarioEmpresarialAdmin && uea.id === idClienteAdmin && (ae.estado inSet obtenerListaEstados(estadoUsuario))
          }
        } yield (agenteEmpresarial))

        val usuariosEmpresarialesLista = new ListBuffer[UsuarioEmpresarial]()
        if ((correoUsuario == null || correoUsuario.isEmpty) && (usuario == null || usuario.isEmpty) && (nombreUsuario == null || nombreUsuario.isEmpty))
          usuariosEmpresarialesLista ++= usuariosQuery.list
        else {
          if (correoUsuario != null && !correoUsuario.isEmpty && usuariosQuery != null) {
            if (usuariosQuery.filter(_.correo === correoUsuario).exists.run)
              usuariosQuery = usuariosQuery.filter(_.correo === correoUsuario)
            else
              usuariosQuery = null
          }
          if (usuario != null && !usuario.isEmpty && usuariosQuery != null) {
            if (usuariosQuery.filter(_.usuario === usuario).exists.run)
              usuariosQuery = usuariosQuery.filter(_.usuario === usuario)
            else
              usuariosQuery = null
          }
          if (nombreUsuario != null && !nombreUsuario.isEmpty && usuariosQuery != null) {
            if (usuariosQuery.filter(_.nombreUsuario === nombreUsuario).exists.run)
              usuariosQuery = usuariosQuery.filter(_.nombreUsuario === nombreUsuario)
            else
              usuariosQuery = null
          }
          if (usuariosQuery != null)
            usuariosEmpresarialesLista ++= usuariosQuery.list
        }
        usuariosEmpresarialesLista.toList
      }
      resolveTry(resultTry, "Consulta agentes empresariales que pertenezcan a la empresa del cliente administrador y cumpla con parametros de busqueda")
  }

  private def obtenerListaEstados(estadoUsuarioBusqueda: Int): List[Int] = {
    if (estadoUsuarioBusqueda == -1)
      List(0, 1, 2, 3, 4)
    else
      List(estadoUsuarioBusqueda)
  }

  def cambiarPassword(idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {
        u <- usuariosEmpresariales.filter(_.id === idUsuario)
      } yield (u.contrasena, u.numeroIngresosErroneos)
      val resultTry = Try {
        query.update((Some(password), 0))
      }
      resolveTry(resultTry, "Cambiar la contraseña de usuario agente empresarial")
  }

  def actualizarEstadoUsuario(idUsuario: Int, estado: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { usuariosEmpresariales.filter(_.id === idUsuario).map(_.estado).update(estado) }
      resolveTry(resultTry, "Actualizar estado de usuario agente empresarial")
  }

  def consultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = Try { usuariosEmpresarialesAdmin.filter(x => x.id === idUsuario && x.contrasena === pw_actual).list.headOption }
      resolveTry(resultTry, "Consulta contrasena actual de cliente admin  " + pw_actual)
  }

  def actualizarContrasena(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for { u <- usuariosEmpresarialesAdmin if u.id === idUsuario } yield (u.contrasena, u.fechaActualizacion)
      val fechaAct = new org.joda.time.DateTime().getMillis
      val act = (Some(pw_nuevo), new Timestamp(fechaAct))
      val resultTry = Try {
        query.update(act)
      }
      resolveTry(resultTry, "Actualizar Contrasena clientes admin y fecha de actualizacion")
  }

  def consultaContrasenaActualAgenteEmpresarial(pw_actual: String, idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarial]]] = loan {
    implicit session =>
      val resultTry = Try { usuariosEmpresariales.filter(x => x.id === idUsuario && x.contrasena === pw_actual).list.headOption }
      resolveTry(resultTry, "Consulta contrasena actual de cliente admin  " + pw_actual)
  }

  def actualizarContrasenaAgenteEmpresarial(pw_nuevo: String, idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val query = for {
        u <- usuariosEmpresariales if u.id === idUsuario
      } yield (u.contrasena, u.fechaActualizacion, u.numeroIngresosErroneos)
      val fechaAct = new org.joda.time.DateTime().getMillis
      val act = (Some(pw_nuevo), new Timestamp(fechaAct), 0)
      val resultTry = Try {
        query.update(act)
      }
      resolveTry(resultTry, "Actualizar Contrasena agente empresariales y fecha de actualizacion")
  }

  def asociarPerfiles(perfiles: List[PerfilAgenteAgente]): Future[Validation[PersistenceException, List[Int]]] = loan {
    implicit session =>
      val resultTry = Try { (perfilesAgentes ++= perfiles).toList }
      resolveTry(resultTry, "Asociar perfiles del cliente administrador")
  }

  def caducarFechaUltimoCambioContrasenaAgenteEmpresarial(idUsuario: Int): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val calendar = Calendar.getInstance()
      calendar.clear(Calendar.YEAR)
      val time = calendar.getTimeInMillis
      val timestamp = new Timestamp(time)
      val query = for { u <- usuariosEmpresariales if u.id === idUsuario } yield u.fechaActualizacion
      val resultTry = Try { query.update(timestamp) }
      resolveTry(resultTry, "Caducar Contrasena usuario Agente empresarial")
  }

  def obtieneClientePorNitYUsuario(nit: String, usuario: String): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      resolveTry(Try(usuariosEmpresarialesAdmin.filter(u => u.identificacion === nit && u.usuario === usuario).list.headOption), "Consulta cliente admin")
  }

}
