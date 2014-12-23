package co.com.alianza.infrastructure.messages

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import spray.http.StatusCode

trait MessageService

case class InboxMessage() extends MessageService

case class ResponseMessage( statusCode:StatusCode, responseBody:String = "" )