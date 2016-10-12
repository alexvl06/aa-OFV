package portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario

import java.sql.Timestamp

import co.com.alianza.constants.LlavesReglaContrasena
import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._
import co.com.alianza.exceptions.{PersistenceException, ValidacionException, ValidacionExceptionPasswordRules}
import co.com.alianza.persistence.entities.{UltimaContrasenaAgenteInmobiliario, UsuarioAgenteInmobiliario}
import co.com.alianza.util.clave.{Crypto, ErrorValidacionClave, ValidarClave}
import co.com.alianza.util.token.Token
import enumerations.{AppendPasswordUser, EstadosUsuarioEnumInmobiliario, PerfilesUsuario, UsoPinEmpresaEnum}
import org.joda.time.DateTime
import portal.transaccional.autenticacion.service.drivers.reglas.ReglaContrasenaRepository
import portal.transaccional.autenticacion.service.drivers.usuarioAgenteInmobiliario.{UsuarioInmobiliarioPinRepository, UsuarioInmobiliarioRepository}
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UltimaContraseñaAgenteInmobiliarioDAOs

import scala.concurrent.Future
import scalaz.{Validation, Failure => zFailure, Success => zSuccess}

/**
 * Created by s4n in 2016
 */
case class ContrasenaAgenteInmobiliarioDriverRepository(agenteRepo: UsuarioInmobiliarioRepository, oldPassDAO: UltimaContraseñaAgenteInmobiliarioDAOs,
    reglaRepo: ReglaContrasenaRepository, pinRepo: UsuarioInmobiliarioPinRepository) extends ContrasenaAgenteInmobiliarioRepository {

  def actualizarContrasenaCaducada(token: Option[String], pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]): Future[Int] = {
    (token, idUsuario) match {
      case (Some(tk), None) => validarToken(tk).flatMap (idAgente => actualizarContrasena(None, pw_actual, pw_nuevo, idAgente))
      case (None, Some(idAgente)) => actualizarContrasena(None, pw_actual, pw_nuevo, idAgente)
      case _ => Future.failed(ValidacionException("409.69", "Solo uno de los campos token, idUsuario deben ser provistos"))
    }
  }

  def actualizarContrasenaPin(pinHash: String, nuevaContrasena: String, contrasenaActualOp: Option[String]): Future[Int] = {
    pinRepo.validarPinAgente(pinHash).flatMap {
      case Left(_) => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
      case Right(pin) => pin.uso match {
        case uso if uso == UsoPinEmpresaEnum.usoReinicioContrasena.id => agenteRepo.getAgenteInmobiliario(pin.idAgente).flatMap {
          case None => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
          case Some(agente) => actualizarContrasena(Some(pinHash), contrasenaActualOp.getOrElse(""), nuevaContrasena, agente.id)
        }
        case uso if uso == UsoPinEmpresaEnum.creacionAgenteInmobiliario.id => agenteRepo.getAgenteInmobiliario(pin.idAgente).flatMap {
          case None => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
          case Some(agente) => definirContrasena(pinHash, nuevaContrasena, agente)
        }
        case _ => Future.failed(ValidacionException("409.11", "Pin invalido y/o vencido"))
      }
    }
  }

  private def actualizarContrasena(pinHash: Option[String], contrasenaActual: String, nuevaContrasena: String, idAgente: Int): Future[Int] = {
    val hashContrasenaActual: String = Crypto.hashSha512(contrasenaActual.concat(AppendPasswordUser.appendUsuariosFiducia), idAgente)
    val hashNuevaContrasena: String = Crypto.hashSha512(nuevaContrasena.concat(AppendPasswordUser.appendUsuariosFiducia), idAgente)
    for {
      agente <- agenteRepo.getContrasena(hashContrasenaActual, idAgente)
      validacionReglas <- validacionReglasClave(nuevaContrasena, idAgente, PerfilesUsuario.agenteEmpresarial)
      cantRepetidas <- reglaRepo.getRegla(LlavesReglaContrasena.ULTIMAS_CONTRASENAS_NO_VALIDAS.llave)
      contrasenasViejas <- validarContrasenasAnteriores(cantRepetidas.valor.toInt, idAgente, hashNuevaContrasena, hashContrasenaActual)
      contrasenaActual <- agenteRepo.updateContrasena(hashNuevaContrasena, idAgente)
      ultimasContrasenas <- oldPassDAO.create(UltimaContrasenaAgenteInmobiliario(None, idAgente, hashContrasenaActual, new Timestamp(new DateTime().getMillis)))
      actualizacionEstado <- agenteRepo.updateEstadoAgente(agente.identificacion, agente.usuario, EstadosUsuarioEnumInmobiliario.activo)
      eliminarPin <- if (pinHash.nonEmpty) pinRepo.eliminarPinAgente(pinHash.get) else Future.successful(0)
    } yield idAgente
  }

  private def definirContrasena(pinHash: String, nuevaContrasena: String, agente: UsuarioAgenteInmobiliario): Future[Int] = {
    val hashContrasena: String = Crypto.hashSha512(nuevaContrasena.concat(AppendPasswordUser.appendUsuariosFiducia), agente.id)
    for {
      validacionReglas <- validacionReglasClave(nuevaContrasena, agente.id, PerfilesUsuario.agenteEmpresarial)
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

  private def validacionReglasClave(contrasena: String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario): Future[String] = {
    val usuarioFuture = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*)
    usuarioFuture.flatMap {
      case e: Validation[PersistenceException, List[ErrorValidacionClave]] =>
        e match {
          case zSuccess(a) => if (a.isEmpty) Future.successful("True") else Future.failed(ValidacionExceptionPasswordRules("409.5", "Error Clave",
            a.map(_.toString + "-").toString().replace("List(", "").replace(")", "").replace(",", "").replace(" ", ""), "", ""))
          case zFailure(_) => Future.failed(ValidacionException("409.5", "Contraseña MAL 1"))
        }
      case _ => Future.failed(ValidacionException("409.5", "Contraseña MAL 2"))
    }
  }

  private def validarContrasenasAnteriores(cantContraseñasAnteriores: Int, id: Int, passNew: String, passOld: String) = {
    for {
      contrasenasViejas <- oldPassDAO.findById(cantContraseñasAnteriores, id)
      validarToken <- if (!contrasenasViejas.exists(_.contrasena == passNew) && passNew != passOld) {
        Future.successful(true)
      } else {
        Future.failed(ValidacionExceptionPasswordRules("409.5", "Error clave", "ErrorUltimasContrasenas-", "", ""))
      }
    } yield validarToken
  }
}
