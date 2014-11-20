package co.com.alianza.domain.aggregates.usuarios

import co.com.alianza.exceptions.PersistenceException

import scalaz.{Failure => zFailure, Success => zSuccess, Validation}
import co.com.alianza.infrastructure.messages.{ErrorMessage, UsuarioMessage}
import co.com.alianza.persistence.messages.ConsultaClienteRequest
import scala.concurrent.{ExecutionContext, Future}
import co.com.alianza.infrastructure.dto.{Cliente, Usuario}
import co.com.alianza.infrastructure.anticorruption.clientes.{DataAccessAdapter => DataAccessAdapterCliente }
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => DataAccessAdapterUsuario }

import enumerations.{TipoIdentificacion, EstadosCliente}

import scalaz.Validation.FlatMap._

import co.com.alianza.util.clave.{Crypto, ErrorValidacionClave, ValidarClave}
import co.com.alianza.util.captcha.ValidarCaptcha
import co.com.alianza.app.MainActors
import com.typesafe.config.{ConfigFactory, Config}


/**
 *
 * @author smontanez
 */
object  ValdiacionesUsuario {

  import co.com.alianza.util.json.MarshallableImplicits._
  implicit val _: ExecutionContext = MainActors.dataAccesEx
  implicit private val config: Config = MainActors.conf

  def validacionReglasClave(message:UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {

    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(message.contrasena, None, ValidarClave.reglasGenerales: _*)

    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:List[ErrorValidacionClave]) => x match{
        case List() => zSuccess(Unit)
        case erroresList =>
          val errores = erroresList.foldLeft("") ( (z, i) => i.toString + "-" + z  )
          zFailure(ErrorFormatoClave(errorClave(errores)))
      }
    })
  }


  def validacionReglasClave(contrasena:String, idUsuario: Int): Future[Validation[ErrorValidacion, Unit.type]] = {

    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), ValidarClave.reglasGenerales: _*)

    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:List[ErrorValidacionClave]) => x match{
        case List() => zSuccess(Unit)
        case erroresList =>
          val errores = erroresList.foldLeft("") ( (z, i) => i.toString + "-" + z  )
          zFailure(ErrorFormatoClave(errorClave(errores)))
      }
    })
  }


  def validaCaptcha(message:UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {
    val validador = new ValidarCaptcha()

    val validacionFuture = validador.validarCaptcha(message.clientIp.get, message.challenge,message.uresponse)

    validacionFuture.map(_.leftMap(pe => ErrorCaptcha(errorCaptcha)).flatMap{
      (x:Boolean) => x match{
        case true => zSuccess(Unit)
        case _ => zFailure(ErrorCaptcha(errorCaptcha))
      }
    })
  }


  def validacionConsultaNumDoc(message:UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {
    val usuarioFuture = DataAccessAdapterUsuario.obtenerUsuarioNumeroIdentificacion(message.identificacion)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[Usuario]) => x match{
        case None => zSuccess(Unit)
        case _ => zFailure(ErrorDocumentoExiste(errorUsuarioExiste))
      }
    })
  }

  def validacionConsultaCorreo(message:UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {
    val usuarioFuture = DataAccessAdapterUsuario.obtenerUsuarioCorreo(message.correo)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[Usuario]) => x match{
        case None => zSuccess(Unit)
        case _ => zFailure(ErrorCorreoExiste(errorUsuarioCorreoExiste))
      }
    })
  }

  def validacionConsultaCliente(message:UsuarioMessage): Future[Validation[ErrorValidacion, Cliente]] = {
    val usuarioFuture = DataAccessAdapterCliente.consultarCliente(ConsultaClienteRequest(message.tipoIdentificacion, message.identificacion))
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[Cliente]) => x match{
        case None => zFailure(ErrorClienteNoExiste(errorClienteNoExiste))
        case Some(c) =>
          if(c.wcli_estado == EstadosCliente.bloqueoContraseña)
            zFailure(ErrorClienteNoExiste(errorClienteInactivo))
          else if(getTipoPersona(message) != c.wcli_person)
            zFailure(ErrorClienteNoExiste(errorClienteNoExiste))
          else
            zSuccess(c)
      }
    })
  }

  private def getTipoPersona(message:UsuarioMessage):String = {
    message.tipoIdentificacion match{
      case  TipoIdentificacion.CEDULA_CUIDADANIA.identificador => "N"
      case  TipoIdentificacion.CEDULA_EXTRANJERIA.identificador => "N"
      case _ => "J"
    }
  }

  def validacionConsultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[ErrorValidacion, Option[Usuario]]] = {
    val contrasenaActualFuture = DataAccessAdapterUsuario.ConsultaContrasenaActual(Crypto.hashSha512(pw_actual), idUsuario)
    contrasenaActualFuture.map(_.leftMap(pe => ErrorPersistence(pe.message,pe)).flatMap{
      (x:Option[Usuario]) => x match{
        case Some(c) => zSuccess(x)
        case None => zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
        case _ => zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
      }
    })
  }

  private val errorUsuarioExiste = ErrorMessage("409.1", "Usuario ya existe", "Usuario ya existe").toJson
  private val errorUsuarioCorreoExiste = ErrorMessage("409.3", "Correo ya existe", "Correo ya existe").toJson
  private val errorClienteNoExiste = ErrorMessage("409.2", "No existe el cliente", "No existe el cliente").toJson
  private val errorClienteInactivo = ErrorMessage("409.4", "Cliente inactivo", "Cliente inactivo").toJson
  private def errorClave(error:String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorCaptcha = ErrorMessage("409.6", "Valor captcha incorrecto", "Valor captcha incorrecto").toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson

}
