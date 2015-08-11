package co.com.alianza.domain.aggregates.pin

import java.security.MessageDigest
import java.util.Date

import co.com.alianza.app.MainActors
import co.com.alianza.constants.TiposConfiguracion
import co.com.alianza.domain.aggregates.usuarios.{ErrorPersistence, ErrorValidacion, ErrorPin}
import co.com.alianza.exceptions.{BusinessLevel, PersistenceException}
import co.com.alianza.infrastructure.anticorruption.configuraciones.{DataAccessTranslator => dataAccessTransConf, DataAccessAdapter => dataAccesAdaptarConf}
import co.com.alianza.infrastructure.dto.{PinUsuarioAgenteEmpresarial, Configuracion, PinUsuario, PinUsuarioEmpresarialAdmin}
import co.com.alianza.infrastructure.messages.{ErrorMessage, ResponseMessage}
import co.com.alianza.util.json.MarshallableImplicits._
import spray.http.StatusCodes._

import scala.util.{Success, Failure}
import scalaz.{Failure => zFailure, Success => zSuccess}
import scala.concurrent.{ExecutionContext, Future}
import scalaz.Validation
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import enumerations.{EstadosEmpresaEnum, EstadosUsuarioEnum}
import co.com.alianza.infrastructure.anticorruption.usuarios.{DataAccessAdapter => uDataAccessAdapter}

object PinUtil {

  implicit val ex: ExecutionContext = MainActors.dataAccesEx

  def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md = MessageDigest.getInstance("SHA-512")
    val hash = md.digest( s"""${pin} - ${fechaExpiracion}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    hexString.toString
  }

  def validarPin(response: Option[PinUsuario], funcionalidad:Int ) = {
   response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime){
            //Se comprueba que la funcionalidad desde donde se genero el PIN es Olvido de Contrasena, para actualizar el estado del
            //Usuario
            val futureConsultaUsuarios: Future[Validation[PersistenceException, Int]] = funcionalidad match {
              case 1 =>  uDataAccessAdapter.actualizarEstadoUsuario(valueResponse.idUsuario, EstadosUsuarioEnum.pendienteReinicio.id)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "La funcionalidad no permite cambio de estado del usuario al que pertenece el PIN")))
            }
            ResponseMessage(OK)
          }
          else
            ResponseMessage(Conflict, errorPinNoEncontrado)
        }
        else {
          ResponseMessage(Conflict, errorPinNoEncontrado)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontrado)
    }
  }

  def validarPinUsuarioEmpresarialAdmin(response: Option[PinUsuarioEmpresarialAdmin], funcionalidad: Int) = {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime){
            val futureConsultaUsuarios: Future[Validation[PersistenceException, Int]] = funcionalidad match {
              case 1 =>  uDataAccessAdapter.actualizarEstadoUsuarioEmpresarialAdmin(valueResponse.idUsuario, EstadosEmpresaEnum.pendienteReiniciarContrasena.id)
              case _ => Future.successful(Validation.failure(PersistenceException(new Exception, BusinessLevel, "La funcionalidad no permite cambio de estado del usuario al que pertenece el PIN")))
            }
            ResponseMessage(OK)
          }
          else
            ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
        }
        else {
          ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontradoClienteAdmin)
    }
  }

  def validarPinFuture(response: Option[PinUsuario]): Future[Validation[ErrorValidacion, PinUsuario]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontrado))
        }
        else zFailure(ErrorPin(errorPinNoEncontrado))
      case None => zFailure(ErrorPin(errorPinNoEncontrado))
    }
  }

  def validarPinUsuarioEmpresarialAdminFuture(response: Option[PinUsuarioEmpresarialAdmin]): Future[Validation[ErrorValidacion, PinUsuarioEmpresarialAdmin]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
        }
        else zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
      case None => zFailure(ErrorPin(errorPinNoEncontradoClienteAdmin))
    }
  }

  def validarPinUsuarioAgenteEmpresarial(response: Option[PinUsuarioAgenteEmpresarial]) = {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime)
            ResponseMessage(OK)
          else
            ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
        }
        else {
          ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
        }
      case None => ResponseMessage(Conflict, errorPinNoEncontradoAgenteEmpresarial)
    }
  }

  def validarPinUsuarioAgenteEmpresarialFuture(response: Option[PinUsuarioAgenteEmpresarial]): Future[Validation[ErrorValidacion, PinUsuarioAgenteEmpresarial]] = Future {
    response match {
      case Some(valueResponse) =>
        val pinHash = deserializarPin(valueResponse.token, valueResponse.fechaExpiracion)
        if (pinHash == valueResponse.tokenHash) {
          val fecha = new Date()
          if (fecha.getTime < valueResponse.fechaExpiracion.getTime) zSuccess(valueResponse)
          else zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
        }
        else zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
      case None => zFailure(ErrorPin(errorPinNoEncontradoAgenteEmpresarial))
    }
  }

  private val errorPinNoEncontrado = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasena/1\" target=\"_blank\" >AQUÍ</a>.").toJson
  private val errorPinNoEncontradoAgenteEmpresarial = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/\" target=\"_blank\" >AQUÍ</a>.").toJson
  private val errorPinNoEncontradoClienteAdmin = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasena/2\" target=\"_blank\" >AQUÍ</a>.").toJson

}
