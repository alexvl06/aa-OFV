package co.com.alianza.web.empresa

import akka.actor.{ ActorSelection, ActorSystem }
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{ DataAccessAdapter => DataAccessAdapterAgenteEmpresarial }
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => DataAccessAdapterClienteAdmin }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import spray.http.StatusCodes
import spray.routing.{ Directives, RequestContext }

import scala.concurrent.ExecutionContext

/**
 * Created by S4N on 17/12/14.
 */
case class AdministrarContrasenaEmpresaService(kafkaActor: ActorSelection, contrasenasAgenteEmpresarialActor: ActorSelection,
    contrasenasClienteAdminActor: ActorSelection)(implicit val system: ActorSystem) extends Directives with AlianzaCommons {

  import system.dispatcher
  import AdministrarContrasenaEmpresaMessagesJsonSupport._

  def secureRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      //TODO: esta validacion no va acá !!
      if (user.tipoCliente.eq(TiposCliente.comercialSAC))
        complete((StatusCodes.Unauthorized, "Tipo usuario SAC no está autorizado para realizar esta acción"))
      else
        path("reiniciarContrasena") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                //Cambiar contrasena de la cuenta alianza valores
                entity(as[ReiniciarContrasenaAgenteEMessage]) {
                  data =>
                    clientIP {
                      ip =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, ReiniciarContrasenaAgenteEMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.reiniciarContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                        } {
                          val dataAux: ReiniciarContrasenaAgenteEMessage = data.copy(nit = Some(user.identificacion))
                          requestExecute(dataAux, contrasenasAgenteEmpresarialActor)
                        }
                    }
                }
              }
            }
          }
        } ~ pathPrefix("bloquearDesbloquearAgente") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                entity(as[BloquearDesbloquearAgenteEMessage]) {
                  data =>
                    clientIP {
                      ip =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, BloquearDesbloquearAgenteEMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.bloqueoAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                        } {
                          val dataAux: BloquearDesbloquearAgenteEMessage = data.copy(idClienteAdmin = Some(user.id))
                          requestExecute(dataAux, contrasenasAgenteEmpresarialActor)
                        }
                    }
                }
              }
            }
          }
        } ~ pathPrefix("actualizarPwClienteAdmin") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                //Cambiar contrasena por el usuario cliente admin
                entity(as[CambiarContrasenaClienteAdminMessage]) {
                  data =>
                    clientIP {
                      ip =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, CambiarContrasenaClienteAdminMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaClienteAdministradorIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_nuevo = null, pw_actual = null)))
                        } {
                          val dataComplete: CambiarContrasenaClienteAdminMessage = data.copy(idUsuario = Some(user.id))
                          requestExecute(dataComplete, contrasenasClienteAdminActor)
                        }
                    }
                }
              }
            }
          }
        } ~ pathPrefix("actualizarPwAgenteEmpresarial") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                //Cambiar contrasena por el usuario agente empresarial
                entity(as[CambiarContrasenaAgenteEmpresarialMessage]) {
                  data =>
                    clientIP {
                      ip =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterAgenteEmpresarial.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, CambiarContrasenaAgenteEmpresarialMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data.copy(pw_nuevo = null, pw_actual = null)))
                        } {
                          val dataComplete: CambiarContrasenaAgenteEmpresarialMessage = data.copy(idUsuario = Some(user.id))
                          requestExecute(dataComplete, contrasenasAgenteEmpresarialActor)
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
