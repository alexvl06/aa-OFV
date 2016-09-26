package portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario

import java.sql.Timestamp

import co.com.alianza.domain.aggregates.empresa.ValidacionesAgenteEmpresarial._
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.persistence.entities.UltimaContrasenaAgenteInmobiliario
import co.com.alianza.util.clave.{ Crypto, ErrorValidacionClave, ValidarClave }
import co.com.alianza.util.token.Token
import enumerations.{ AppendPasswordUser, PerfilesUsuario }
import org.joda.time.DateTime
import portal.transaccional.autenticacion.service.drivers.usuarioInmobiliario.UsuarioInmobiliarioRepository
import portal.transaccional.fiduciaria.autenticacion.storage.daos.portal.UltimaContraseñaAgenteInmobiliarioDAOs

import scala.concurrent.Future
import scalaz.{ Validation, Failure => zFailure, Success => zSuccess }

/**
 * Created by s4n in 2016
 */
case class ContrasenaAgenteInmobiliarioDriverRepository(agenteRepo : UsuarioInmobiliarioRepository, oldPassDAO : UltimaContraseñaAgenteInmobiliarioDAOs)
  extends ContrasenaAgenteInmobiliarioRepository {

  def actualizarContrasena (token: String, pw_actual: String, pw_nuevo: String, idUsuario: Option[Int]): Future[Int] = {

    val passwordActual = Crypto.hashSha512(pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario.getOrElse(0))
    val passwordNew = Crypto.hashSha512(pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia), idUsuario.getOrElse(0))

    for {
      us_id <- validarToken(token)
      usuarioContrasenaActual <- agenteRepo.getContrasena(passwordActual, us_id)
      idValReglasContra <- validacionReglasClave2(pw_nuevo, us_id, PerfilesUsuario.agenteEmpresarial)
      columnasActualizadas <- agenteRepo.updateContrasena(passwordNew, us_id)
      resultGuardarUltimasContrasenas <- oldPassDAO.create(UltimaContrasenaAgenteInmobiliario(None, us_id,passwordActual,new Timestamp(new DateTime().getMillis)))
    } yield us_id
  }

  private def validarToken (token: String): Future[Int] = {
    if(Token.autorizarToken(token)) {
      Future.successful(Token.getToken(token).getJWTClaimsSet.getCustomClaim("us_id").toString.toInt)
    } else {
      Future.failed(ValidacionException("409.11", "Token invalido"))
    }
  }

  //{"code":"409.5","title":"Error clave","detail":"ErrorMinCaracteresEsp-ErrorMinMayusculas-ErrorMinMinusculas-","time":"2016/09/26 16:50","data":null}

  def validacionReglasClave2(contrasena: String, idUsuario: Int, perfilUsuario: PerfilesUsuario.perfilUsuario): Future[String] = {
    val usuarioFuture = ValidarClave.aplicarReglas(contrasena, Some(idUsuario), perfilUsuario, ValidarClave.reglasGenerales: _*)
    usuarioFuture.flatMap{
      case e : Validation[PersistenceException, List[ErrorValidacionClave]] =>
        e match {
          case zSuccess(a) => if(a.isEmpty) Future.successful("True") else Future.failed(ValidacionExceptionPasswordRules("409.5","Error Clave",
            a.map(_.toString + "-").toString().replace("List(","").replace(")","").replace(",","").replace(" ",""), "",""))
          case zFailure(_) => Future.failed(ValidacionException("409.5", "Contraseña MAL 1"))
        }
      case _ => Future.failed(ValidacionException("409.5", "Contraseña MAL 2"))
    }
  }
}
