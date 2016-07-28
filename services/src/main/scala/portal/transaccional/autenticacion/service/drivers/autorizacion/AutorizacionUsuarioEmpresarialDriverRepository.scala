package portal.transaccional.autenticacion.service.drivers.autorizacion

import co.com.alianza.domain.aggregates.autenticacion.errores.{ ErrorAutorizacion, ErrorPersistenciaAutorizacion }
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.dto.UsuarioEmpresarial
import co.com.alianza.infrastructure.messages.AutorizarUsuarioEmpresarialMessage
import co.com.alianza.util.token.{ AesUtil, Token }
import enumerations.CryptoAesParameters
import portal.transaccional.autenticacion.service.drivers.usuario.UsuarioEmpresarialDriverRepository

import scala.concurrent.Future
import scalaz.Validation

/**
 * Created by s4n on 2016
 */
case class AutorizacionUsuarioEmpresarialDriverRepository(agenteRepo: UsuarioEmpresarialDriverRepository) {

  //  def autorizar (token: String, url: Option[String], ip: String) = {
  //    for {
  //      token <- agenteRepo.actualizarToken(message)
  //      sesion <- obtieneSesion(message.token)
  //      tuplaUsuarioOptionEstadoEmpresa <- obtieneUsuarioEmpresarial(message.token)
  //      validUs <- validarUsuario(tuplaUsuarioOptionEstadoEmpresa match { case None => None case _ => Some(tuplaUsuarioOptionEstadoEmpresa.get._1) })
  //      validacionEstadoEmpresa <- validarEstadoEmpresa(tuplaUsuarioOptionEstadoEmpresa match {
  //        case None => None case _ =>
  //          Some(tuplaUsuarioOptionEstadoEmpresa.get._2)
  //      })
  //      validacionIp <- validarIpEmpresa(sesion, message.ip))
  //      result <- autorizarRecursoAgente(tuplaUsuarioOptionEstadoEmpresa match {
  //        case None => None case _ =>
  //          Some(tuplaUsuarioOptionEstadoEmpresa.get._1)
  //      }, message.url)
  //    } yield {
  //      result
  //    }).run
  //  }
  //
  //  private def validarToken(message: AutorizarUsuarioEmpresarialMessage): Future[Validation[ErrorAutorizacion, Option[UsuarioEmpresarial]]] = {
  //    var util = new AesUtil(CryptoAesParameters.KEY_SIZE, CryptoAesParameters.ITERATION_COUNT)
  //    var decryptedToken = util.decrypt(CryptoAesParameters.SALT, CryptoAesParameters.IV, CryptoAesParameters.PASSPHRASE, message.token)
  //    Token.autorizarToken(decryptedToken) match {
  //      case true =>
  //        agenteRepo.obtenerUsuarioEmpresarialToken(message.token).flatMap { x =>
  //          val y: Validation[PersistenceException, Future[Option[UsuarioEmpresarial]]] = x.map { userOpt =>
  //            guardaTokenCache(userOpt match { case None => None case _ => Some(userOpt.get._1) }, message)
  //          }
  //          co.com.alianza.util.transformers.Validation.sequence(y).map(_.leftMap { pe => ErrorPersistenciaAutorizacion(pe.message, pe) })
  //        }
  //      case false =>
  //        Future.successful(Validation.success(None))
  //    }
  //  }
}
