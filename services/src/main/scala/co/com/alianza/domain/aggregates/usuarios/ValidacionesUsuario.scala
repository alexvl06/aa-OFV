package co.com.alianza.domain.aggregates.usuarios

import co.com.alianza.constants.{ TiposConfiguracion }
import co.com.alianza.exceptions.{ PersistenceException, ServiceException }
import spray.http.StatusCodes._

import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }
import co.com.alianza.infrastructure.messages.{ ErrorMessage, ResponseMessage, UsuarioMessage }

import scala.concurrent.{ ExecutionContext, Future }
import co.com.alianza.infrastructure.dto.{ Cliente, Configuracion, Usuario }
import co.com.alianza.infrastructure.anticorruption.clientes.{ DataAccessAdapter => DataAccessAdapterCliente }
import co.com.alianza.infrastructure.anticorruption.usuarios.{ DataAccessAdapter => DataAccessAdapterUsuario }
import co.com.alianza.infrastructure.anticorruption.configuraciones.{ DataAccessAdapter => dataAccesAdaptarConf }
import enumerations.{ EstadosCliente, PerfilesUsuario, TipoIdentificacion }

import scalaz.Validation.FlatMap._
import co.com.alianza.util.clave.{ Crypto, ErrorValidacionClave, ValidarClave }
import co.com.alianza.util.captcha.ValidarCaptcha

import com.typesafe.config.Config

/**
 *
 * @author smontanez
 */
object ValidacionesUsuario {

  import co.com.alianza.util.json.MarshallableImplicits._
  implicit val _: ExecutionContext = MainActors.dataAccesEx
  implicit private val config: Config = MainActors.conf

  def validacionReglasClaveAutoregistro(message: UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {

    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(message.contrasena, None, PerfilesUsuario.clienteIndividual, ValidarClave.reglasGeneralesAutoregistro: _*)

    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: List[ErrorValidacionClave]) =>
        x match {
          case List() => zSuccess(Unit)
          case erroresList: List[ErrorValidacionClave] =>
            val errores = erroresList.foldLeft("")((z, i) => i.toString + "-" + z)
            zFailure(ErrorFormatoClave(errorClave(errores)))
        }
    })
  }

  def validacionReglasClave(message: UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {

    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(message.contrasena, None, PerfilesUsuario.clienteIndividual, ValidarClave.reglasGenerales: _*)

    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: List[ErrorValidacionClave]) =>
        x match {
          case List() => zSuccess(Unit)
          case erroresList =>
            val errores = erroresList.foldLeft("")((z, i) => i.toString + "-" + z)
            zFailure(ErrorFormatoClave(errorClave(errores)))
        }
    })
  }

  def validacionReglasClave(contrasena: String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario): Future[Validation[ErrorValidacion, Unit.type]] = {
    val usuarioFuture: Future[Validation[PersistenceException, List[ErrorValidacionClave]]] = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: List[ErrorValidacionClave]) =>
        x match {
          case List() => zSuccess(Unit)
          case erroresList =>
            val errores = erroresList.foldLeft("")((z, i) => i.toString + "-" + z)
            zFailure(ErrorFormatoClave(errorClave(errores)))
        }
    })
  }

  def validaCaptcha(message: UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {
    val validador = new ValidarCaptcha()
    val validacionFuture = validador.validarCaptcha(message.clientIp.get, message.challenge, message.uresponse)

    validacionFuture.map(_.leftMap(pe => ErrorCaptcha(errorCaptcha)).flatMap {
      (x: Boolean) =>
        x match {
          case true => zSuccess(Unit)
          case _ => zFailure(ErrorCaptcha(errorCaptcha))
        }
    })
  }

  def validacionConsultaNumDoc(message: UsuarioMessage): Future[Validation[ErrorValidacion, Option[Usuario]]] = {
    val usuarioFuture = DataAccessAdapterUsuario.obtenerUsuarioNumeroIdentificacion(message.identificacion)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[Usuario]) =>
        x match {
          case None => zSuccess(x)
          case _ => zFailure(ErrorDocumentoExiste(errorUsuarioExiste))
        }
    })
  }

  def validacionUsuarioNumDoc(identificacion: String): Future[Validation[ErrorValidacion, Option[Usuario]]] = {
    val usuarioFuture = DataAccessAdapterUsuario.obtenerUsuarioNumeroIdentificacion(identificacion)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[Usuario]) =>
        x match {
          case Some(usuarioEncontrado) => zSuccess(Some(usuarioEncontrado))
          case _ => zFailure(ErrorUsuarioNoExiste(errorUsuarioNoExiste))
        }
    })
  }

  def validacionConsultaCorreo(message: UsuarioMessage): Future[Validation[ErrorValidacion, Unit.type]] = {
    val usuarioFuture = DataAccessAdapterUsuario.obtenerUsuarioCorreo(message.correo)
    usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[Usuario]) =>
        x match {
          case None => zSuccess(Unit)
          case _ => zFailure(ErrorCorreoExiste(errorUsuarioCorreoExiste))
        }
    })
  }

  def validacionConsultaCliente(identificacion: String, tipoIdentificacion: Int, validarCorreo: Boolean): Future[Validation[ErrorValidacion, Cliente]] = {
    if (tipoIdentificacion != TipoIdentificacion.GRUPO.identificador) {
      val usuarioFuture = DataAccessAdapterCliente.consultarCliente(identificacion)
      usuarioFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
        (x: Option[Cliente]) =>
          x match {
            case None => zFailure(ErrorClienteNoExiste(errorClienteNoExiste))
            case Some(cliente) =>
              validacionConsultaCliente(cliente, tipoIdentificacion, validarCorreo)
          }
      })
    } else {
      val grupoFuture = DataAccessAdapterCliente.consultarGrupo(identificacion.toInt)
      grupoFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
        (x: Option[Cliente]) =>
          x match {
            case None =>
              zFailure(ErrorClienteNoExiste(errorClienteNoExiste))
            case Some(cliente) =>
              validacionConsultaCliente(cliente, tipoIdentificacion, validarCorreo)
          }
      })
    }
  }

  def validacionConsultaCliente(cliente: Cliente, tipoPersona: Int, validarCorreo: Boolean): Validation[ErrorValidacion, Cliente] = {
    if (cliente.wcli_estado != EstadosCliente.inactivo && cliente.wcli_estado != EstadosCliente.bloqueado && cliente.wcli_estado != EstadosCliente.activo)
      zFailure(ErrorClienteNoExiste(errorClienteInactivo))
    else if (getTipoPersona(tipoPersona) != cliente.wcli_person)
      zFailure(ErrorClienteNoExiste(errorClienteNoExiste))
    else if (validarCorreo && (cliente.wcli_dir_correo == null || cliente.wcli_dir_correo.isEmpty))
      zFailure(ErrorClienteNoExiste(errorCorreoNoExiste))
    else
      zSuccess(cliente)
  }

  private def getTipoPersona(tipoIdentificacion: Int): String = {
    tipoIdentificacion match {
      case TipoIdentificacion.FID.identificador => "F"
      case TipoIdentificacion.NIT.identificador => "J"
      case TipoIdentificacion.GRUPO.identificador => "G"
      case TipoIdentificacion.SOCIEDAD_EXTRANJERA.identificador => "J"
      case _ => "N"
    }
  }

  def validacionConsultaContrasenaActual(pw_actual: String, idUsuario: Int): Future[Validation[ErrorValidacion, Option[Usuario]]] = {
    val contrasenaActualFuture = DataAccessAdapterUsuario.consultaContrasenaActual(Crypto.hashSha512(pw_actual, idUsuario), idUsuario)
    contrasenaActualFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[Usuario]) =>
        x match {
          case Some(c) => zSuccess(x)
          case None => zFailure(ErrorContrasenaNoExiste(errorContrasenaActualNoExiste))
        }
    })
  }

  def validacionConsultaTiempoExpiracion(): Future[Validation[ErrorValidacion, Configuracion]] = {
    val configuracionFuture = dataAccesAdaptarConf.obtenerConfiguracionPorLlave(TiposConfiguracion.EXPIRACION_PIN.llave)
    configuracionFuture.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)).flatMap {
      (x: Option[Configuracion]) =>
        x match {
          case Some(c) => zSuccess(c)
          case None => zFailure(ErrorPin(errorPin))
        }
    })
  }

  def validaIpConfianza(ipConfianza: Boolean): Future[Validation[ErrorValidacion, Unit.type]] = Future {
    ipConfianza match {
      case true => zSuccess(Unit)
      case _ => zFailure(ErrorIpConfianza(errorIpConfianza))
    }
  }

  def errorValidacion(error: Any): Any = {
    error match {
      case errorPersistence: ErrorPersistence => errorPersistence.exception
      case errorVal: ErrorValidacion => ResponseMessage(Conflict, errorVal.msg)
      case _ => error
    }
  }

  def toErrorValidation[T](futuro: Future[Validation[PersistenceException, T]]): Future[Validation[ErrorValidacion, T]] = {
    futuro.map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  def toErrorValidationCorreo[T](futuro: Future[Validation[ServiceException, T]]): Future[Validation[ErrorValidacion, T]] = {
    futuro.map(_.leftMap(ps => ErrorCorreo("Error enviando correo")))
  }

  private val errorUsuarioExiste = ErrorMessage("409.1", "Usuario ya existe", "Usuario ya existe").toJson
  private val errorClienteNoExiste = ErrorMessage("409.2", "No existe el cliente", "No existe el cliente").toJson
  private val errorUsuarioCorreoExiste = ErrorMessage("409.3", "Correo ya existe", "Correo ya existe").toJson
  private val errorClienteInactivo = ErrorMessage("409.4", "Cliente inactivo", "Cliente inactivo").toJson
  private def errorClave(error: String) = ErrorMessage("409.5", "Error clave", error).toJson
  private val errorCaptcha = ErrorMessage("409.6", "Valor captcha incorrecto", "Valor captcha incorrecto").toJson
  private val errorContrasenaActualNoExiste = ErrorMessage("409.7", "No existe la contrasena actual", "No existe la contrasena actual").toJson
  private val errorPin = ErrorMessage("409.8", "Error en el pin", "Ocurrió un error al obtener el tiempo de expiracion del pin").toJson
  private val errorUsuarioNoExiste = ErrorMessage("409.9", "No existe el usuario", "No existe el usuario").toJson
  private val errorCorreoNoExiste = ErrorMessage("409.10", "No hay correo registrado", "No hay correo registrado en la base de datos de Alianza").toJson
  private val errorIpConfianza = ErrorMessage("409.11", "La Ip no es de confianza", "La Ip no es de confianza").toJson

}
