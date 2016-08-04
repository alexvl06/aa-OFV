package portal.transaccional.autenticacion.service.web.ip

import co.com.alianza.app.CrossHeaders
import co.com.alianza.commons.enumerations.TiposCliente
import co.com.alianza.infrastructure.dto.security.UsuarioAuth
import portal.transaccional.autenticacion.service.drivers.ip.IpRepository
import portal.transaccional.autenticacion.service.util.JsonFormatters.DomainJsonFormatters
import portal.transaccional.autenticacion.service.util.ws.CommonRESTFul
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

/**
 * Created by s4n on 2016
 */
case class IpService(user: UsuarioAuth, ipRepo : IpRepository)(implicit val ec: ExecutionContext) extends CommonRESTFul with DomainJsonFormatters
  with CrossHeaders {

  val ponerIpHabitual = "ponerIpHabitual"

  val route : Route = {
    path(ponerIpHabitual) {
      pathEndOrSingleSlash {
        guardar()
      }
    }
  }

  private def guardar () = {
      post {
        entity(as[AgregarIpRequest]) {
          ponerIpHabitual =>
              clientIP { ip =>
                val resultado  = user.tipoCliente match {
                      case TiposCliente.clienteIndividual => ipRepo.agregarIpHabitualUsuario(user.identificacionUsuario, ip.value)
                      case _ => ipRepo.agregarIPHabitualUsuarioEmpresarialAdmin(user.id, ip.value)
                    }

                onComplete(resultado) {
                  case Success(value) => complete("Registro de IP Exitoso")
                  case Failure(ex) => complete((StatusCodes.Unauthorized, "El usuario no esta autorizado para registrar ip"))
                }
              }
        }
      }
  }

//    ~ path("actualizarInactividad") {
//      post {
//        complete { "ok" }
//      }
//    }
//  }
}
