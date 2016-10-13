package portal.transaccional.autenticacion.service.web.enumeracion

import akka.actor.ActorSystem
import co.com.alianza.app.{AlianzaCommons, CrossHeaders}
import co.com.alianza.util.json.JsonUtil
import enumerations.TipoIdentificacion
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.routing.Directives

class EnumeracionService(implicit val system: ActorSystem)  extends CommonRESTFul with DomainJsonFormatters with CrossHeaders  {

  val enumeracion = "enumeracion"
  val tiposIdentificacion = "tiposIdentificacion"
  val tiposIdentificacionNatural = "tiposIdentificacionNatural"
  val tiposIdentificacionEmpresas = "tiposIdentificacionEmpresas"

  def route = {
    path(enumeracion / tiposIdentificacion) {
      get {
        complete {
          TipoIdentificacion.obtenerTodos()
        }
      }
    } ~ path(enumeracion / tiposIdentificacionNatural) {
      get {
        complete {
          TipoIdentificacion.obtenerTiposIdentificacionNatural()
        }
      }
    } ~ path(enumeracion / tiposIdentificacionEmpresas) {
      get {
        complete {
          TipoIdentificacion.obtenerTiposIdentificacionEmpresas()
        }
      }
    }
  }
}