package co.com.alianza.web

import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages.{ActualizacionMessagesJsonSupport, ObtenerCiudades, ObtenerPaises}
import spray.http.StatusCodes
import spray.routing.Directives

/**
 * Created by david on 16/06/14.
 */
class ActualizacionService extends Directives with AlianzaCommons with CrossHeaders {

  val actualizacion = "actualizacion"
  val paises = "paises"
  val ciudades = "ciudades"

  import ActualizacionMessagesJsonSupport._

  def route/*(user: UsuarioAuth)*/ = {
    pathPrefix(actualizacion) {
      get {
        respondWithMediaType(mediaType) {
          pathPrefix(paises){
            requestExecute(new ObtenerPaises, actualizacionActor)
          } ~
          pathPrefix(ciudades){
            requestExecute(new ObtenerCiudades, actualizacionActor)
          }
        }
      } /*~
      put {
        entity(as[AgregarIpsUsuarioMessage]) {
          agregarIpsUsuarioMessage =>
            respondWithMediaType(mediaType) {
              val agregarIpsUsuarioMessageAux: AgregarIpsUsuarioMessage = agregarIpsUsuarioMessage.copy(idUsuario = Some(user.id), tipoCliente = Some(user.tipoCliente.id))
              requestExecute(agregarIpsUsuarioMessageAux, ipsUsuarioActor)
            }
        }
      }*/
    }
  }

}
