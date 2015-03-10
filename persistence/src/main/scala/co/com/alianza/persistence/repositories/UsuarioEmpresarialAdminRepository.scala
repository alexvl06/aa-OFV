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

  val pinusuariosEmpresarialesAdmin = TableQuery[PinUsuarioEmpresarialAdminTable]

  def obtenerUsuarioToken( token:String ): Future[Validation[PersistenceException, Option[UsuarioEmpresarialAdmin]]] = loan {
    implicit session =>
      val resultTry = Try{ usuariosEmpresarialesAdmin.filter(_.token === token).list.headOption}
      resolveTry(resultTry, "Consulta usuario empresarial admin por token: " + token)
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

  def existeUsuarioEmpresarialAdminActivo(nitEmpresa:String): Future[Validation[PersistenceException, Boolean]] = loan {
    implicit session =>
      val resultTry = Try{
        val estado = EstadosEmpresaEnum.activo.id
        usuariosEmpresarialesAdmin.filter( u =>u.identificacion === nitEmpresa && u.estado === estado).exists.run
      }
      resolveTry(resultTry, "Validacion: existe usuario empresarial admin activo")
  }

}