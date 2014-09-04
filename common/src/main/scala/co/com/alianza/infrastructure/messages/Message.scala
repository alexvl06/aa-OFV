package co.com.alianza.infrastructure.messages

import spray.http.StatusCode
import spray.http.HttpMethod
import spray.http.HttpMethods._
trait MessageService


case class CacheMessage(key:String, verb:HttpMethod = GET) extends MessageService

case class ResponseMessage(statusCode:StatusCode, responseBody:String = "")

case class FileResponseMessage(file:Array[Byte],fileName:String)