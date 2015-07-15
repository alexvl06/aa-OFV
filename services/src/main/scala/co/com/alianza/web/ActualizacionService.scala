package co.com.alianza.web

import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import co.com.alianza.infrastructure.messages._
import spray.http.StatusCodes
import spray.routing.Directives

/**
 * Created by david on 16/06/14.
 */
class ActualizacionService extends Directives with AlianzaCommons with CrossHeaders {

  val paises = "paises"
  val ciudades = "ciudades"
  val tiposCorreo = "tiposCorreo"
  val ocupaciones = "ocupaciones"
  val actualizacion = "actualizacion"
  val envioCorrespondencia = "envioCorrespondencia"
  val actividadesEconomicas = "actividadesEconomicas"

  import ActualizacionMessagesJsonSupport._

  def route/*(user: UsuarioAuth)*/ = {
    pathPrefix(actualizacion) {
      get {
        respondWithMediaType(mediaType) {
          pathPrefix(paises){
            requestExecute(new ObtenerPaises, actualizacionActor)
          } ~
          pathPrefix(ciudades / IntNumber){
            (pais : Int) =>
              requestExecute(new ObtenerCiudades(pais), actualizacionActor)
          } ~
          pathPrefix(tiposCorreo){
            requestExecute(new ObtenerTiposCorreo, actualizacionActor)
          } ~
          pathPrefix(envioCorrespondencia){
            requestExecute(new ObtenerEnvioCorrespondencia, actualizacionActor)
          } ~
          pathPrefix(ocupaciones){
            requestExecute(new ObtenerOcupaciones, actualizacionActor)
          } ~
          pathPrefix(actividadesEconomicas){
            requestExecute(new ObtenerActividadesEconomicas, actualizacionActor)
          }
        }
      }
    }


      /*~
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
