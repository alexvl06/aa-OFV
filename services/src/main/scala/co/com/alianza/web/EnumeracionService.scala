package co.com.alianza.web

import spray.routing.Directives
import spray.http.StatusCodes._
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.AutenticacionMessagesJsonSupport
import co.com.alianza.infrastructure.messages.AutenticarMessage
import co.com.alianza.util.json.JsonUtil
import enumerations.{TipoIdentificaciones, TipoIdentificacion}
;


class EnumeracionService extends Directives with AlianzaCommons  {

  val enumeracion = "enumeracion"
  val tiposIdentificacion = "tiposIdentificacion"

  def route= {
    path(enumeracion/tiposIdentificacion) {
        get {
            complete {
              val list = List(new TipoIdentificaciones( 1 ,"CC" ),new TipoIdentificaciones( 2 ,"CE" ), new TipoIdentificaciones( 3 ,"NIT" ))
              JsonUtil.toJson(
                list
              );
            }
        } ~
          // mapHttpResponseEntity(jsonFilter){
          get {
            complete(OK)
          }

        //}
      }
    }
}