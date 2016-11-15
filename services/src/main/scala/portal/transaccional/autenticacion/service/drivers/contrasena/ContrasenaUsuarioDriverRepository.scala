package portal.transaccional.autenticacion.service.drivers.contrasena

import co.com.alianza.exceptions.ValidacionException
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser

import scala.concurrent.Future

/**
 * Created by hernando on 9/11/16.
 */
case class ContrasenaUsuarioDriverRepository() {

  def validarContrasenaActual(idUsuario: Int, contrasena: String, contrasenaActualHash: String): Future[Boolean] = {
    val contrasenaHash: String = Crypto.hashSha512(contrasena.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario)
    contrasenaHash.equals(contrasenaActualHash) match {
      case true => Future.successful(true)
      case _ => Future.failed(ValidacionException("409.7", "No existe la contrasena actual"))
    }
  }

  /*
  * //TODO: Verificar si el metodo CambiarContrasenaMessage se está utilizando
    case message: CambiarContrasenaMessage =>
      val currentSender = sender()
      val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
      val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
      val CambiarContrasenaFuture = (for {
        usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, message.idUsuario.get))
        idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, message.idUsuario.get, PerfilesUsuario.clienteIndividual))
        idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
        resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(message.idUsuario.get, Crypto.hashSha512(passwordNewAppend, message.idUsuario.get)))
      } yield {
        idUsuario
      }).run
      resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)

    //TODO: Verificar si el metodo CambiarContrasenaCaducadaMessage se está utilizando
    case message: CambiarContrasenaCaducadaMessage =>
      val currentSender = sender()
      val tk_validation = Token.autorizarToken(message.token)
      tk_validation match {
        case true =>
          val claim = Token.getToken(message.token).getJWTClaimsSet()
          val us_id = claim.getCustomClaim("us_id").toString.toInt
          val us_tipo = claim.getCustomClaim("us_tipo").toString
          val passwordActualAppend = message.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)
          val passwordNewAppend = message.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)
          val CambiarContrasenaFuture = (for {
            usuarioContrasenaActual <- ValidationT(validacionConsultaContrasenaActual(passwordActualAppend, us_id))
            idValReglasContra <- ValidationT(validacionReglasClave(message.pw_nuevo, us_id, PerfilesUsuario.clienteIndividual))
            idUsuario <- ValidationT(actualizarContrasena(passwordNewAppend, usuarioContrasenaActual))
            resultGuardarUltimasContrasenas <- ValidationT(guardarUltimaContrasena(us_id, Crypto.hashSha512(passwordNewAppend, message.us_id)))
          } yield {
            idUsuario
          }).run
          resolveCambiarContrasenaFuture(CambiarContrasenaFuture, currentSender)
        case false => currentSender ! ResponseMessage(Conflict, tokenValidationFailure)
      }

  }

  private def guardarUltimaContrasena(idUsuario: Int, uContrasena: String): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapterUltimaContrasena.guardarUltimaContrasena(UltimaContrasena(None, idUsuario, uContrasena, new Timestamp(System.currentTimeMillis()))).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def actualizarContrasena(pw_nuevo: String, usuario: Option[Usuario]): Future[Validation[ErrorValidacion, Int]] = {
    DataAccessAdapter.actualizarContrasena(pw_nuevo, usuario.get.id.get).map(_.leftMap(pe => ErrorPersistence(pe.message, pe)))
  }

  private def resolveCambiarContrasenaFuture(CambiarContrasenaFuture: Future[Validation[ErrorValidacion, Int]], currentSender: ActorRef) = {
    CambiarContrasenaFuture onComplete {
      case sFailure(failure) =>
        currentSender ! failure
      case sSuccess(value) =>
        value match {
          case zSuccess(response: Int) =>
            currentSender ! ResponseMessage(OK, response.toJson)
          case zFailure(error) =>
            error match {
              case errorPersistence: ErrorPersistence => currentSender ! errorPersistence.exception
              case errorVal: ErrorValidacion =>
                currentSender ! ResponseMessage(Conflict, errorVal.msg)
            }
        }
    }
  }

  private val tokenValidationFailure = ErrorMessage("409.11", "Token invalido", "El token de caducidad es invalido").toJson
  * */

}
