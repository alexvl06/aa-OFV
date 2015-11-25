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
  val tiposIdentificacionNatural = "tiposIdentificacionNatural"
  val tiposIdentificacionEmpresas = "tiposIdentificacionEmpresas"

  def route= {
    path(enumeracion/tiposIdentificacion) {
        get {
            complete {
              val list = List(new TipoIdentificaciones( 1 ,"CC" ),new TipoIdentificaciones( 2 ,"CE" ), new TipoIdentificaciones( 5 ,"TI" ), new TipoIdentificaciones( 6, "NUIP" ), new TipoIdentificaciones( 7, "Pasaporte" ), new TipoIdentificaciones( 8, "Registro Civil" ))
              JsonUtil.toJson(list)
            }
        }
      }~ path(enumeracion/tiposIdentificacionNatural) {
        get {
          complete {
            val list = List(new TipoIdentificaciones( 1 ,"CC" ),new TipoIdentificaciones( 2 ,"CE" ))
            JsonUtil.toJson(list)
          }
        }
      }~ path(enumeracion/tiposIdentificacionEmpresas) {
      get {
        complete {
          val list = List(new TipoIdentificaciones( 3 ,"NIT" ), new TipoIdentificaciones( 4 ,"FID" ), new TipoIdentificaciones( 9 ,"Sociedad Extranjera" ))
          JsonUtil.toJson(list)
        }
      }
    }
  }
}