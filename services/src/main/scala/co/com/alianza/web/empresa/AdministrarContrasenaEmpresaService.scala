package co.com.alianza.web.empresa

import co.com.alianza.app.AlianzaCommons
import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.anticorruption.usuariosAgenteEmpresarial.{DataAccessAdapter => DataAccessAdapterAgenteEmpresarial}
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{DataAccessAdapter => DataAccessAdapterClienteAdmin}
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import spray.routing.{RequestContext, Directives}

/**
 * Created by S4N on 17/12/14.
 */
class AdministrarContrasenaEmpresaService extends Directives with AlianzaCommons {

  import AdministrarContrasenaEmpresaMessagesJsonSupport._

  def secureRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
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
                        val dataAux: ReiniciarContrasenaAgenteEMessage = data.copy(idClienteAdmin = Some(user.id))
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
                  val dataAux: BloquearDesbloquearAgenteEMessage = data.copy(idClienteAdmin = Some(user.id))
                  requestExecute(dataAux, contrasenasAgenteEmpresarialActor)
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
                          requestWithFutureAuditing[PersistenceException, CambiarContrasenaClienteAdminMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaClienteAdministradorIndex, ip.value, kafkaActor, usuario, Some(data.copy( pw_nuevo = Crypto.hashSha512(data.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)), pw_actual = Crypto.hashSha512(data.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)))))
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
                            requestWithFutureAuditing[PersistenceException, CambiarContrasenaAgenteEmpresarialMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data.copy( pw_nuevo = Crypto.hashSha512(data.pw_nuevo.concat(AppendPasswordUser.appendUsuariosFiducia)), pw_actual = Crypto.hashSha512(data.pw_actual.concat(AppendPasswordUser.appendUsuariosFiducia)))))
                        } {
                        val dataComplete: CambiarContrasenaAgenteEmpresarialMessage = data.copy(idUsuario = Some(user.id))
                        requestExecute(dataComplete, contrasenasAgenteEmpresarialActor)
                      }
                  }
              }
            }
          }
        }
      } ~ pathPrefix("asignarContrasena") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                entity(as[AsignarContrasenaMessage]) {
                  data =>
                  clientIP {
                    ip =>
                      mapRequestContext {
                        r: RequestContext =>
                          val token = r.request.headers.find(header => header.name equals "token")
                          val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                          requestWithFutureAuditing[PersistenceException, AsignarContrasenaMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.asignarContrasenaAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                      } {
                        requestExecute(data, contrasenasAgenteEmpresarialActor)
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
