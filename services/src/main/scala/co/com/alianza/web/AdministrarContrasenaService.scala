package co.com.alianza.web

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.{ PersistenceException, ValidacionException, ValidacionExceptionPasswordRules }
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa.{ CambiarContrasenaCaducadaAgenteEmpresarialMessage, CambiarContrasenaCaducadaClienteAdminMessage }
import co.com.alianza.infrastructure.messages.{ AdministrarContrasenaMessagesJsonSupport, CambiarContrasenaCaducadaMessage, CambiarContrasenaMessage, _ }
import co.com.alianza.util.token.Token
import portal.transaccional.autenticacion.service.drivers.contrasenaAgenteInmobiliario.ContrasenaAgenteInmobiliarioRepository
import spray.http.StatusCodes
import spray.routing.{ Directives, RequestContext, StandardRoute }

import scala.util.{ Failure, Success }

/**
 * Created by seven4n on 01/09/14.
 */
case class AdministrarContrasenaService(kafkaActor: ActorSelection, contrasenasActor: ActorSelection, contrasenasAgenteEmpresarialActor: ActorSelection,
  contrasenasClienteAdminActor: ActorSelection, agenteInmobContrasenaRepo: ContrasenaAgenteInmobiliarioRepository)(implicit val system: ActorSystem) extends Directives with AlianzaCommons {

  import AdministrarContrasenaMessagesJsonSupport._
  import system.dispatcher

  def secureRoute(user: UsuarioAuth) =
    pathPrefix("actualizarContrasena") {
      respondWithMediaType(mediaType) {
        pathEndOrSingleSlash {
          put {
            clientIP {
              ip =>
                //Cambiar contrasena de la cuenta alianza valores
                entity(as[CambiarContrasenaMessage]) {
                  data =>
                    mapRequestContext {
                      r: RequestContext =>
                        val token = r.request.headers.find(header => header.name equals "token")
                        val usuario = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)

                        requestWithFutureAuditing[PersistenceException, CambiarContrasenaMessage](r, AuditingHelper.fiduciariaTopic,
                          AuditingHelper.cambioContrasenaIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_actual = null, pw_nuevo = null)))
                    } {
                      val dataComplete: CambiarContrasenaMessage = data.copy(idUsuario = Some(user.id))
                      requestExecute(dataComplete, contrasenasActor)
                    }
                }
            }
          }
        }
      }
    }

  def insecureRoute = {
    pathPrefix("actualizarContrasenaCaducada") {
      respondWithMediaType(mediaType) {
        pathEndOrSingleSlash {
          put {
            entity(as[CambiarContrasenaCaducadaRequestMessage]) {
              data =>
                {

                  val claim = Token.getToken(data.token).getJWTClaimsSet
                  val us_id = claim.getCustomClaim("us_id").toString.toInt
                  val us_tipo = claim.getCustomClaim("us_tipo").toString
                  val tipoCliente = TiposCliente.withName(us_tipo)

                  tipoCliente match {
                    case TiposCliente.agenteEmpresarial =>
                      requestExecute(CambiarContrasenaCaducadaAgenteEmpresarialMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasAgenteEmpresarialActor)
                    case TiposCliente.clienteAdministrador | TiposCliente.clienteAdminInmobiliario =>
                      requestExecute(CambiarContrasenaCaducadaClienteAdminMessage(data.token, data.pw_actual, data.pw_nuevo, Some(us_id)), contrasenasClienteAdminActor)
                    case TiposCliente.clienteIndividual =>
                      requestExecute(CambiarContrasenaCaducadaMessage(data.token, data.pw_actual, data.pw_nuevo, us_id, us_tipo), contrasenasActor)
                    case TiposCliente.agenteInmobiliario => {
                      val response = agenteInmobContrasenaRepo.actualizarContrasenaCaducada(Option(data.token), data.pw_actual, data.pw_nuevo, Option(us_id))
                      onComplete(response) {
                        case Success(resultado) => complete(StatusCodes.OK)
                        case Failure(ex) => execution(ex)
                      }
                    }

                  }
                }
            }
          }
        }
      }
    }
  }

  def execution(ex: Any): StandardRoute = {
    ex match {
      case ex: ValidacionException => println(StatusCodes.OK); complete((StatusCodes.Conflict, ex))
      case ex: PersistenceException => complete((StatusCodes.InternalServerError, "Error inesperado"))
      case ex: ValidacionExceptionPasswordRules => complete((StatusCodes.Conflict, ex))
      case ex: Throwable => complete((StatusCodes.InternalServerError, "Error inesperado"))
    }
  }

}
