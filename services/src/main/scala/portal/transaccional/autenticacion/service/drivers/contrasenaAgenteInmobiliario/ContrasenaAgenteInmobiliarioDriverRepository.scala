package portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario

import java.sql.Timestamp

import co.com.alianza.constants.LlavesReglaContrasena
import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._
import co.com.alianza.exceptions.{ ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.persistence.entities.{ UltimaContrasenaAgenteInmobiliario, UsuarioAgenteInmobiliario }
import co.com.alianza.util.clave.{ Crypto, ValidarClave }
import co.com.alianza.util.token.Token
import enumerations.{ AppendPasswordUser, EstadosUsuarioEnumInmobiliario, PerfilesUsuario, UsoPinEmpresaEnum }
import org.joda.time.DateTime
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.{ UsuarioInmobiliarioPinRepository, UsuarioInmobiliarioRepository }
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UltimaContraseĆ±aAgenteInmobiliarioDAOs

import scala.concurrent.Future
import scalaz.{ Failure => zFailure, Success => zSuccess }

/**
 * Created by s4n in 2016
 */
case class ContrasenaAgenteInmobiliarioDriverRepository(agenteRepo: UsuarioInmobiliarioRepository, oldPassDAO: UltimaContraseĆ±aAgenteInmobiliarioDAOs,
    reglaRepo: ReglaContrasenaRepository, pinRepo: UsuarioInmobiliarioPinRepository) extends ContrasenaAgenteInmobiliarioRepository {

  def actualizarContrasenaCaducada(token: Option[String], pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]): Future[Int] = {
    (token, idUsuario) match {
      case (Some(tk), None) => validarToken(tk).flatMap(idAgente => actualizarContrasena(None, pw_actual, pw_nuevo, idAgente, valoresRegla = false))
      case (None, Some(idAgente)) => actualizarContrasena(None, pw_actual, pw_nuevo, idAgente, valoresRegla = true)
      case _ => Future.failed(ValidacionException("409.69", "Solo uno de los campos token, idUsuario deben ser provistos"))
    }
  }

  def actualizarContrasenaPin(pinHash: String, nuevaContrasena: String): Future[Int] = {
    pinRepo.validarPinAgente(pinHash).flatMap {
      case Left(_) => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
      case Right(pin) => pin.uso match {
        case uso if uso == UsoPinEmpresaEnum.usoReinicioContrasena.id => agenteRepo.getAgenteInmobiliario(pin.idAgente).flatMap {
          case None => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
          case Some(agente) if agente.contrasena.isEmpty => definirContrasena(pinHash, nuevaContrasena, agente)
          case Some(agente) =>
            actualizarContrasena(Some(pinHash), agente.contrasena.get, nuevaContrasena, agente.id, valoresRegla = true, Some(agente))
        }
        case uso if uso == UsoPinEmpresaEnum.creacionAgenteInmobiliario.id => agenteRepo.getAgenteInmobiliario(pin.idAgente).flatMap {
          case None => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
          case Some(agente) => definirContrasena(pinHash, nuevaContrasena, agente)
        }
      }
    }
  }

  private def actualizarContrasena(pinHash: Option[String], contrasenaActual: String, nuevaContrasena: String,
    idAgente: Int, valoresRegla: Boolean, agenteOp: Option[UsuarioAgenteInmobiliario] = None): Future[Int] = {
    val hashContrasenaActual: String = Crypto.hashSha512(contrasenaActual.concat(AppendPasswordUser.appendUsuariosFiducia), idAgente)
    val hashNuevaContrasena: String = Crypto.hashSha512(nuevaContrasena.concat(AppendPasswordUser.appendUsuariosFiducia), idAgente)

    for {
      agente <- if (agenteOp.isEmpty) agenteRepo.getContrasena(hashContrasenaActual, idAgente) else Future.successful(agenteOp.get)
      validacionReglas <- validacionReglasClave(nuevaContrasena, idAgente, PerfilesUsuario.agenteEmpresarial, valoresRegla)
      cantRepetidas <- reglaRepo.getRegla(LlavesReglaContrasena.ULTIMAS_CONTRASENAS_NO_VALIDAS)
      contrasenasViejas <- validarContrasenasAnteriores(cantRepetidas.valor.toInt, idAgente, hashNuevaContrasena, agente.contrasena.get, valoresRegla)
      contrasenaActual <- agenteRepo.updateContrasena(hashNuevaContrasena, idAgente)
      ultimasContrasenas <- oldPassDAO.create(UltimaContrasenaAgenteInmobiliario(None, idAgente, hashNuevaContrasena, new Timestamp(new DateTime().getMillis)))
      actualizacionEstado <- agenteRepo.updateEstadoAgente(agente.identificacion, agente.usuario, EstadosUsuarioEnumInmobiliario.activo)
      eliminarPin <- if (pinHash.nonEmpty) pinRepo.eliminarPinAgente(pinHash.get) else Future.successful(0)
    } yield idAgente
  }

  private def definirContrasena(pinHash: String, nuevaContrasena: String, agente: UsuarioAgenteInmobiliario): Future[Int] = {
    val hashContrasena: String = Crypto.hashSha512(nuevaContrasena.concat(AppendPasswordUser.appendUsuariosFiducia), agente.id)
    for {
      validacionReglas <- validacionReglasClave(nuevaContrasena, agente.id, PerfilesUsuario.agenteEmpresarial, valoresRegla = true)
      actualizacionContrasena <- agenteRepo.updateContrasena(hashContrasena, agente.id)
      ultimasContrasenas <- oldPassDAO.create(UltimaContrasenaAgenteInmobiliario(None, agente.id, hashContrasena, new Timestamp(new DateTime().getMillis)))
      actualizacionEstado <- agenteRepo.updateEstadoAgente(agente.identificacion, agente.usuario, EstadosUsuarioEnumInmobiliario.activo)
      eliminarPin <- pinRepo.eliminarPinAgente(pinHash)
    } yield agente.id
  }

  private def validarToken(token: String): Future[Int] = {
    if (Token.autorizarToken(token)) {
      Future.successful(Token.getToken(token).getJWTClaimsSet.getCustomClaim("us_id").toString.toInt)
    } else {
      Future.failed(ValidacionException("409.11", "Token invalido"))
    }
  }

  private def validacionReglasClave(contrasena: String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario, valoresRegla: Boolean) = {

    val sucess = Future.successful("True")
    val error = Future.failed(ValidacionException("409.5", "Error de persistencia ..."))
    val code = "409.5"

    valoresRegla match {
      case true =>
        ValidarClave.aplicarReglasValor(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*).flatMap {
          case zSuccess(erroresContrasena) => erroresContrasena.isEmpty match {
            case true => sucess
            case false => Future.failed(ValidacionException(code, erroresContrasena.map(error => s"${error._1.toString}:${error._2}").mkString("\u0000")))
          }
          case zFailure => error
        }
      case false =>
        ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*).flatMap {
          case zSuccess(erroresContrasena) => erroresContrasena.isEmpty match {
            case true => sucess
            case false => Future.failed(ValidacionExceptionPasswordRules(code, "Error clave", erroresContrasena.map(error => s"${error.toString}").mkString("-"), "", ""))
          }
          case zFailure => error
        }
    }
  }

  private def validarContrasenasAnteriores(cantContraseĆ±asAnteriores: Int, id: Int, passNew: String, passOld: String, valoresRegla: Boolean) = {
    val code = "409.5"
    val error = "ErrorUltimasContrasenas"

    for {
      contrasenasViejas <- oldPassDAO.findById(cantContraseĆ±asAnteriores, id)
      validarToken <- if (!contrasenasViejas.exists(_.contrasena == passNew) && passNew != passOld) {
        Future.successful(true)
      } else if (valoresRegla) {
        Future.failed(ValidacionException(code, error + ":" + cantContraseĆ±asAnteriores.toString))
      } else {
        Future.failed(ValidacionExceptionPasswordRules(code, "Error clave", error + "-", "", ""))
      }
    } yield validarToken
  }
}
