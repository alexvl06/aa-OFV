package co.com.alianza.persistence.repositories

import java.sql.Timestamp

import enumerations.{EstadosEmpresaEnum, EstadosUsuarioEnum}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.persistence.entities._
import co.com.alianza.exceptions.PersistenceException
import CustomDriver.simple._
import scala.util.Try
import scala.slick.lifted.TableQuery

/**
 * Created by manuel on 18/12/14.
 */
class UsuarioEmpresarialAdminRepository ( implicit executionContext: ExecutionContext) extends AlianzaRepository{


  val usuariosEmpresarialesAdmin = TableQuery[UsuarioEmpresarialAdminTable]
  val UsuariosEmpresarialesAdminEmpresa = TableQuery[UsuarioEmpresarialAdminEmpresaTable]
  val pinusuariosEmpresarialesAdmin = TableQuery[PinUsuarioEmpresarialAdminTable]
  val Empresas = TableQuery[EmpresaTable]

  def obtenerUsuarioToken( token:String ): Future[Validation[PersistenceException, Option[(UsuarioEmpresarialAdmin, Int)]]] = loan {
    implicit session =>
      val query = for {
        (clienteAdministrador, empresa) <-
        usuariosEmpresarialesAdmin join UsuariosEmpresarialesAdminEmpresa on {
          (uea, ueae) => uea.token === token && uea.id === ueae.idUsuarioEmpresarialAdmin
        } join Empresas on {
          case ((uea, ueae), e) => ueae.idEmpresa === e.id
        }
      } yield {
        (clienteAdministrador._1, empresa.estadoEmpresa)
      }

      val resultTry = Try { query.list.headOption }
      resolveTry(resultTry, "Consulta cliente administrador por token: " + token)
  }

  def invalidarTokenUsuario( token:String  ) : Future[Validation[PersistenceException, Int]] = loan {

    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.token === token ).map(_.token ).update(Some(null))  }
      resolveTry(resultTry, "Invalidar token usuario")
  }

  def asociarTokenUsuario( usuarioId: Int, token: String ) : Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === usuarioId ).map(_.token ).update(Some(token))  }
      resolveTry(resultTry, "Actualizar token de usuario empresarial")
  }

  def actualizarEstadoUsuario( idUsuario:Int, estado:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === idUsuario ).map(_.estado ).update(estado)  }
      resolveTry(resultTry, "Actualizar estado de usuario cliente admin")
  }

  def cambiarPassword (idUsuario: Int, password: String): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try { usuariosEmpresarialesAdmin.filter(_.id === idUsuario).map(_.contrasena).update(Some (password)) }
      resolveTry(resultTry, "Cambiar la contraseña de usuario cliente admin")
  }

  def actualizarNumeroIngresosErroneos( idUsuario:Int, numeroIntentos:Int ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === idUsuario ).map(_.numeroIngresosErroneos).update(numeroIntentos )  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en numeroIngresosErroneos ")
  }

  def actualizarIpUltimoIngreso( idUsuario:Int, ipActual:String ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === idUsuario ).map(_.ipUltimoIngreso ).update(Some(ipActual))  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en ipUltimoIngreso ")
  }

  def actualizarFechaUltimoIngreso( idUsuario:Int, fechaActual : Timestamp ): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === idUsuario ).map(_.fechaUltimoIngreso ).update(Some(fechaActual))  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def obtenerUsuarioEmpresarialAdminPorId(idUsuario: Int): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter( _.id === idUsuario ).firstOption  }
      resolveTry(resultTry, "Actualizar usuario empresarial admin en fechaUltimoIngreso ")
  }

  def guardarPinUsuarioClienteAdmin(pinUsuario:PinUsuarioEmpresarialAdmin): Future[Validation[PersistenceException, Int]] = loan {
    implicit session =>
      val resultTry = Try{  (pinusuariosEmpresarialesAdmin returning pinusuariosEmpresarialesAdmin.map(_.id.get)) +=pinUsuario }
      resolveTry(resultTry, "Agregar pin usuario empresarial administrador")
  }

  def obtenerUsuarioPorToken( token:String ): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter(_.token === token).list.headOption}
      resolveTry(resultTry, "Consulta usuario empresarial con token" + token)
  }

  def existeUsuarioEmpresarialAdminActivo(nitEmpresa:String): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val resultTry = Try{
        val estado = EstadosEmpresaEnum.activo.id
        usuariosEmpresarialesAdmin.filter( u =>u.identificacion === nitEmpresa && u.estado === estado).exists.run
      }
      resolveTry(resultTry, "Validacion: existe usuario empresarial admin activo")
  }

}