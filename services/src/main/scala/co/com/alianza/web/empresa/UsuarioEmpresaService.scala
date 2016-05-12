package co.com.alianza.web.empresa

import co.com.alianza.exceptions.PersistenceException
import co.com.alianza.infrastructure.anticorruption.usuarios.DataAccessAdapter
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.messages.GuardarPermisosAgenteMessage
import co.com.alianza.infrastructure.messages.empresa.{ CrearAgenteEMessageJsonSupport, CrearAgenteMessage }
import spray.routing.{ RequestContext, Directives }
import co.com.alianza.app.{ AlianzaActors, CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.messages.empresa._
import co.com.alianza.infrastructure.anticorruption.usuariosClienteAdmin.{ DataAccessAdapter => DataAccessAdapterClienteAdmin }
import co.com.alianza.infrastructure.dto.security.UsuarioAuth

/**
 * Created by s4n on 17/12/14.
 */
class UsuarioEmpresaService extends Directives with AlianzaCommons with CrossHeaders with AlianzaActors {

  import CrearAgenteEMessageJsonSupport._

  def secureUserRouteEmpresa(user: UsuarioAuth) = {
    pathPrefix("empresa") {
      path("consultarUsuarios") {
        respondWithMediaType(mediaType) {
          get {
            parameters('correo.?, 'usuario.?, 'nombre.?, 'estado.?) { (correo, usuario, nombre, estado) =>
              clientIP {
                ip =>
                  mapRequestContext {
                    r: RequestContext =>
                      val token = r.request.headers.find(header => header.name equals "token")
                      val usuario2 = DataAccessAdapter.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                      requestWithFutureAuditing[PersistenceException, AuditParams](r, AuditingHelper.fiduciariaTopic, AuditingHelper.consultaUsuariosEmpresarialesIndex, ip.value, kafkaActor, usuario2, Some(AuditParams(correo, usuario, nombre, estado)))
                  } {
                    requestExecute(GetAgentesEmpresarialesMessage(correo.getOrElse(null), usuario.getOrElse(null), nombre.getOrElse(null), estado.get.toInt, user.id), agenteEmpresarialActor)
                  }
              }
            }
          }
        }
      } ~
        path("usuarioAgenteEmpresarial") {
          respondWithMediaType(mediaType) {
            pathEndOrSingleSlash {
              clientIP {
                ip =>
                  put {
                    entity(as[CrearAgenteMessage]) {
                      data =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, CrearAgenteMessage](r, AuditingHelper.fiduciariaTopic,
                              AuditingHelper.crearAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                        } {
                          requestExecute(data, agenteEmpresarialActor)
                        }
                    }
                  } ~ post {
                    entity(as[ActualizarAgenteMessage]) {
                      data =>
                        mapRequestContext {
                          r: RequestContext =>
                            val token = r.request.headers.find(header => header.name equals "token")
                            val usuario = DataAccessAdapterClienteAdmin.obtenerTipoIdentificacionYNumeroIdentificacionUsuarioToken(token.get.value)
                            requestWithFutureAuditing[PersistenceException, ActualizarAgenteMessage](r, AuditingHelper.fiduciariaTopic,
                              AuditingHelper.editarAgenteEmpresarialIndex, ip.value, kafkaActor, usuario, Some(data))
                        } {
                          requestExecute(data.copy(nit = Some(user.identificacionUsuario)), agenteEmpresarialActor)
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

case class AuditParams(correo: Option[String], usuario: Option[String], nombre: Option[String], estado: Option[String])