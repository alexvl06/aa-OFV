package co.com.alianza.web

import spray.routing.Directives
import spray.http.StatusCodes._
import co.com.alianza.app.AlianzaCommons
import co.com.alianza.infrastructure.messages.AutenticacionMessagesJsonSupport
import co.com.alianza.infrastructure.messages.AutenticarMessage
import co.com.alianza.util.json.JsonUtil
import enumerations.{ TipoIdentificaciones, TipoIdentificacion };

class EnumeracionService extends Directives with AlianzaCommons {

  val enumeracion = "enumeracion"
  val tiposIdentificacion = "tiposIdentificacion"
  val tiposIdentificacionNatural = "tiposIdentificacionNatural"
  val tiposIdentificacionEmpresas = "tiposIdentificacionEmpresas"

  def route = {
    path(enumeracion / tiposIdentificacion) {
      get {
        complete {
          val list = List(new TipoIdentificaciones(1, "Cédula de Ciudadanía"), new TipoIdentificaciones(2, "Cédula de Extranjería"),
            new TipoIdentificaciones(5, "Tarjeta de Identidad"), new TipoIdentificaciones(7, "Pasaporte"),
            new TipoIdentificaciones(8, "Registro Civil"), new TipoIdentificaciones(9, "NUIP"))
          JsonUtil.toJson(list)
        }
      }
    } ~ path(enumeracion / tiposIdentificacionNatural) {
      get {
        complete {
          val list = List(new TipoIdentificaciones(1, "Cédula de Ciudadanía"), new TipoIdentificaciones(2, "Cédula de Extranjería"))
          JsonUtil.toJson(list)
        }
      }
    } ~ path(enumeracion / tiposIdentificacionEmpresas) {
      get {
        complete {
          val list = List(new TipoIdentificaciones(3, "NIT"), new TipoIdentificaciones(4, "FID"), new TipoIdentificaciones(6, "Sociedad Extranjera"), new TipoIdentificaciones(10, "Grupo"))
          JsonUtil.toJson(list)
        }
      }
    }
  }
}