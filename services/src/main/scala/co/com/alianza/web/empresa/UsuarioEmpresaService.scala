package co.com.alianza.web.empresa

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.messages.empresa.{CrearAgenteEMessageJsonSupport, CrearAgenteEMessage}
import spray.routing.{RequestContext, Directives}
import co.com.alianza.app.{AlianzaActors, CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{DataAccessAdapter => DataAccessAdapterClienteAdmin}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons   with CrossHeaders with AlianzaActors {

  import CrearAgenteEMessageJsonSupport._

  def secureUserRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      path("consultarUsuarios") {
        respondWithMediaType(mediaType) {
          get {
            parameters('correo.?, 'usuario.?, 'nombre.?, 'estado.?) { (correo, usuario, nombre, estado) =>
              //Lista de todos los usuarios
              requestExecute(GetAgentesEmpresarialesMessage(correo.getOrElse(null), usuario.getOrElse(null), nombre.getOrElse(null), estado.get.toInt, user.id), agenteEmpresarialActor)
            }
          }
        }
      } ~
        path("usuarioAgenteEmpresarial") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              put {
                entity(as[CrearAgenteEMessage]) {
                  data =>
                    clientIP {
                      ip =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, CrearAgenteEMessage](r, AuditingHelper.fiduciariaTopic, AuditingHelper.crearAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                        } {
                          requestExecute(data, agenteEmpresarialActor)
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
