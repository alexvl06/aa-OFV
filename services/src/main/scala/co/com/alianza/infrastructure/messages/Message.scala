package co.com.alianza.infrastructure.messages

import spray.http.StatusCode

trait MessageService

case class InboxMessage() extends MessageService