package co.com.alianza.webvalidarPinClienteAdmin

import co.com.alianza.app.{CrossHeaders, AlianzaCommons}
import co.com.alianza.infrastructure.messages.PinMessages._
import co.com.alianza.infrastructure.messages.PinMarshallers._
import spray.routing.Directives

class PinService extends Directives with AlianzaCommons with CrossHeaders {

  def route = {
    path("validarPin" / Segment / IntNumber) {
      (pin, ft)  => {
        post {
          requestExecute(ValidarPin(pin, Some(ft)), pinActor)
        }
      }
    } ~ path("validarPinClienteAdmin" / Segment/ IntNumber) {
      (pin, ft) => {
        post {
          requestExecute(ValidarPin(pin, Some(ft)), pinUsuarioEmpresarialAdminActor)
        }
      }
    } ~ path("cambiarPw" / Segment) {
      pin => {
        entity(as[UserPw]) {
          userPw => {
            post {
              requestExecute(CambiarPw(pin, userPw.pw), pinActor)
            }
          }
        }
      }
    } ~ path("cambiarPwClienteAdmin" / Segment) {
      pin => {
        entity(as[UserPw]) {
          userPw => {
            post {
              requestExecute(CambiarPw(pin, userPw.pw), pinUsuarioEmpresarialAdminActor)
            }
          }
        }
      }
    } ~ path("validarPinAgenteEmpresarial" / Segment) {
      pin => {
        post {
          requestExecute(ValidarPin(pin, None), pinUsuarioAgenteEmpresarialActor)
        }
      }
    } ~ path("cambiarPwAgenteEmpresarial" / Segment) {
      pin => {
        entity(as[UserPw]) {
          userPw => {
            post {
              requestExecute(CambiarPw(pin, userPw.pw), pinUsuarioAgenteEmpresarialActor)
            }
          }
        }
      }
    }
  }

}
