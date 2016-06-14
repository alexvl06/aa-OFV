package co.com.alianza.webvalidarPinClienteAdmin

import co.com.alianza.app.{ CrossHeaders, AlianzaCommons }
import co.com.alianza.infrastructure.auditing.AuditingHelper
import co.com.alianza.infrastructure.auditing.AuditingHelper._
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.PinMarshallers._
import co.com.alianza.util.clave.Crypto
import enumerations.AppendPasswordUser
import spray.routing.{ RequestContext, Directives }

class PinService extends Directives with AlianzaCommons with CrossHeaders {

  def route = {
    path("validarPin" / Segment / IntNumber) {
      (pin, ft) =>
        {
          post {
            requestExecute(ValidarPin(pin, Some(ft)), pinActor)
          }
        }
    } ~ path("validarPinClienteAdmin" / Segment / IntNumber) {
      (pin, ft) =>
        {
          post {
            requestExecute(ValidarPin(pin, Some(ft)), pinUsuarioEmpresarialAdminActor)
          }
        }
    } ~ path("cambiarPw" / Segment) {
      pin =>
        {
          entity(as[UserPw]) {
            userPw =>
              {
                post {
                  clientIP {
                    ip =>
                      mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                        AuditingHelper.cambioContrasenaCorreoClienteIndividualIndex, ip.value, kafkaActor, userPw.copy(pw = ""))) {
                        val menssage: CambiarContrasena = userPw.agregarIp match {
                          case Some(true) => CambiarContrasena(pin, userPw.pw, Some(ip.value))
                          case _ => CambiarContrasena(pin, userPw.pw)
                        }
                        requestExecute(menssage, pinActor)
                      }
                  }
                }
              }
          }
        }
    } ~ path("cambiarPwClienteAdmin" / Segment) {
      pin =>
        {
          entity(as[UserPw]) {
            userPw =>
              {
                post {
                  clientIP {
                    ip =>
                      mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic,
                        AuditingHelper.cambioContrasenaCorreoClienteAdministradorIndex, ip.value, kafkaActor, userPw.copy(pw = ""))) {
                        requestExecute(CambiarContrasena(pin, userPw.pw), pinUsuarioEmpresarialAdminActor)
                      }
                  }
                }
              }
          }
        }
    } ~ path("validarPinAgenteEmpresarial" / Segment) {
      pin =>
        {
          post {
            requestExecute(ValidarPin(pin, None), pinUsuarioAgenteEmpresarialActor)
          }
        }
    } ~ path("cambiarPwAgenteEmpresarial" / Segment) {
      pin =>
        {
          entity(as[UserPw]) {
            userPw =>
              {
                post {
                  clientIP {
                    ip =>
                      mapRequestContext((r: RequestContext) => requestWithAuiditing(r, AuditingHelper.fiduciariaTopic, AuditingHelper.cambioContrasenaCorreoAgenteEmpresarialIndex, ip.value, kafkaActor, userPw.copy(pw = null))) {
                        requestExecute(CambiarContrasena(pin, userPw.pw), pinUsuarioAgenteEmpresarialActor)
                      }
                  }
                }
              }
          }
        }
    }
  }

}
