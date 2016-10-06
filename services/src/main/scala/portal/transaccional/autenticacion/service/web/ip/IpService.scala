package portal.transaccional.autenticacion.service.web.ip

import akka.actor.ActorSelection
import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{EliminarIpsUsuarioMessage, AgregarIpsUsuarioMessage, ObtenerIpsUsuarioMessage}
import portal.transaccional.autenticacion.service.drivers.ip.IpRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

/**
 * Created by s4n on 2016
 */
case class IpService(user: UsuarioAuth, kafkaActor: ActorSelection, ipRepo: IpRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
    with CrossHeaders {

  val ponerIpHabitual = "ponerIpHabitual"

  val ipsUsuarios = "ipsUsuarios"

  val route: Route = {
    path(ipsUsuarios) {
      pathEndOrSingleSlash {
        clientIP { ip =>
          obtener(ip.value) ~ eliminar ~ guardar
        }
      }
    } ~ path(ponerIpHabitual) {
      pathEndOrSingleSlash {
        agregarHabitual
      }
    }
  }

  private def obtener(ipPeticion: String) = {
    get{
      mapRequestContext {
        r: RequestContext =>
          requestAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
            ipPeticion, kafkaActor, getAuditingUser(user.tipoIdentificacion, user.identificacion, user.usuario), None)
      } {
        //TODO: poner lo que debe hacer acá

        //requestExecute(new ObtenerIpsUsuarioMessage(user.id, user.tipoCliente), ipsUsuarioActor)
      }
    }
  }



    /*
    * get {
        respondWithMediaType(mediaType) {
          clientIP { ip =>
            mapRequestContext {
              r: RequestContext =>
                val usuario = obtenerUsuario(r, user)
                requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
                  ip.value, kafkaActor, usuario, None)
            } {
              requestExecute(new ObtenerIpsUsuarioMessage(user.id, user.tipoCliente), ipsUsuarioActor)
            }
          }
        }
      } ~
    *
    post {
      clientIP { ip =>
        val resultado: Future[String] = ipRepo.agregarIp(user, ip.value)
        onComplete(resultado) {
          case Success(value) => complete("Registro de IP Exitoso")
          case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para registrar ip"))
        }
    }
  }*/

    private def agregarHabitual() = {
      /*
      * get {
          respondWithMediaType(mediaType) {
            clientIP { ip =>
              mapRequestContext {
                r: RequestContext =>
                  val usuario = obtenerUsuario(r, user)
                  requestWithFutureAuditing[PersistenceException, Any](r, AuditingHelper.fiduciariaTopic, AuditingHelper.usuarioConsultarIpIndex,
                    ip.value, kafkaActor, usuario, None)
              } {
                requestExecute(new ObtenerIpsUsuarioMessage(user.id, user.tipoCliente), ipsUsuarioActor)
              }
            }
          }
        } ~
      * */
      post {
        clientIP { ip =>
          val resultado: Future[String] = ipRepo.agregarIp(user, ip.value)
          onComplete(resultado) {
            case Success(value) => complete("Registro de IP Exitoso")
            case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para registrar ip"))
          }
        }
      }



  private def eliminar() = {
    post {
      entity(as[AgregarIpRequest]) {
        ponerIpHabitual =>
          clientIP { ip =>
            val resultado: Future[String] = ipRepo.agregarIp(user, ip.value)
            onComplete(resultado) {
              case Success(value) => complete("Registro de IP Exitoso")
              case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para registrar ip"))
            }
          }
      }
    }
  }
  private def guardar() = {
    post {
      entity(as[AgregarIpRequest]) {
        ponerIpHabitual =>
          clientIP { ip =>
            val resultado: Future[String] = ipRepo.agregarIp(user, ip.value)
            onComplete(resultado) {
              case Success(value) => complete("Registro de IP Exitoso")
              case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para registrar ip"))
            }
          }
      }
    }
  }

  def route(user: UsuarioAuth) = {
    path(ipsUsuarios) {

        put {
          entity(as[AgregarIpsUsuarioMessage]) {
            agregarIpsUsuarioMessage =>
              respondWithMediaType(mediaType) {
                clientIP { ip =>
                  mapRequestContext {
                    r: RequestContext =>
                      val usuario = obtenerUsuario(r, user)
                      requestWithFutureAuditing[PersistenceException, AgregarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic,
                        AuditingHelper.usuarioAgregarIpIndex, ip.value, kafkaActor, usuario, Some(agregarIpsUsuarioMessage))
                  } {
                    val agregarIpsUsuarioMessageAux = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
                    requestExecute(agregarIpsUsuarioMessageAux, ipsUsuarioActor)
                  }
                }
              }
          }
        } ~
        delete {
          entity(as[EliminarIpsUsuarioMessage]) {
            eliminarIpsUsuarioMessage =>
              respondWithMediaType(mediaType) {
                clientIP { ip =>
                  mapRequestContext {
                    r: RequestContext =>
                      val usuario = obtenerUsuario(r, user)
                      requestWithFutureAuditing[PersistenceException, EliminarIpsUsuarioMessage](r, AuditingHelper.fiduciariaTopic,
                        AuditingHelper.usuarioEliminarIpIndex, ip.value, kafkaActor, usuario, Some(eliminarIpsUsuarioMessage))
                  } {
                    val eliminarIpsUsuarioMessageAux = eliminarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
                    requestExecute(eliminarIpsUsuarioMessageAux, ipsUsuarioActor)
                  }
                }
              }
          }
        }

    }
  }

}