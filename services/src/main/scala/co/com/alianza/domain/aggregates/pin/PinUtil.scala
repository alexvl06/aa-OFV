package co.com.alianza.domain.aggregates.pin

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date

import co.com.alianza.infrastructure.messages.ErrorMessage
import co.com.alianza.persistence.entities.PinAgenteInmobiliario
import co.com.alianza.persistence.util.DataBaseExecutionContext
import co.com.alianza.util.json.MarshallableImplicits._
import enumerations.EstadosPin
import enumerations.EstadosPin.EstadoPin
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext
import scalaz.{Failure => zFailure, Success => zSuccess}

object PinUtil {

  implicit val ec: ExecutionContext = DataBaseExecutionContext.executionContext

  def deserializarPin(pin: String, fechaExpiracion: Date): String = {
    val md = MessageDigest.getInstance("SHA-512")
    //formatear fecha
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
    val fechaFormato: String = format.format(fechaExpiracion)
    val hash = md.digest(s"""${pin} - ${fechaFormato}""".getBytes)
    val hexString = new StringBuffer()
    for (i <- hash) {
      hexString.append(Integer.toHexString(0xFF & i))
    }
    hexString.toString
  }

  /**
   * Verifica la validez de un pin generado para un agente empresaial
   *
   * @param pinOp Pin a validar
   * @return Un Either [CodigoError, Booleano] que indica si el pin es válido o no
   *
   */
  def validarPinAgenteInmobiliario(pinOp: Option[PinAgenteInmobiliario]): Either[EstadoPin, PinAgenteInmobiliario] = {
    pinOp.map { pin =>
      val hash: String = deserializarPin(pin.token, pin.fechaExpiracion.toDate)
      if (hash == pin.tokenHash) {
        val now: DateTime = DateTime.now(DateTimeZone.UTC)
        if (now.isBefore(pin.fechaExpiracion)) {
          Right(pin)
        } else {
          // el pin está caducado
          Left(EstadosPin.PIN_CADUCADO)
        }
      } else {
        // el hash del pin es diferente al hash calculado
        Left(EstadosPin.PIN_INVALIDO)
      }
    }.getOrElse {
      // no existe el pin
      Left(EstadosPin.PIN_NO_EXISTE)
    }
  }

  private val errorPinNoEncontrado = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasena\" target=\"_blank\" >AQUÍ</a>.").toJson
  private val errorPinNoEncontradoAgenteEmpresarial = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela con su cliente administrador.").toJson
  private val errorPinNoEncontradoClienteAdmin = ErrorMessage("409.1", "Pin invalido", "El proceso para la definición de la contraseña está vencido. Si requiere una nueva contraseña solicítela <a href=\"/#!/olvidarContrasenaEmpresa\" target=\"_blank\" >AQUÍ</a>.").toJson

}