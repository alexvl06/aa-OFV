package co.com.alianza.infrastructure.dto.security

import spray.http.{HttpResponse, HttpEntity}
import spray.json._

/**
 * Representa el usuario autenticado
 *
 * @param id El id del usuario autenticado
 */
case class UsuarioAuth(id:Int)

/**
Adaptador para el usuario autenticado
 */
object UsuarioAuthAdapter {
  import DefaultJsonProtocol._
  implicit val marshaller = jsonFormat1( UsuarioAuth )
  def from( obj: HttpEntity ) = {
    val json = obj.asString.asJson
    json.convertTo[UsuarioAuth]
  }
}