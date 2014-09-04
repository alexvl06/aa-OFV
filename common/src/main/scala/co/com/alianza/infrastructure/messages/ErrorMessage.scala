package co.com.alianza.infrastructure.messages

import org.joda.time.format.DateTimeFormat

case class ErrorMessage(code:String, title:String, detail:String, time:String)

object ErrorMessage{

  def apply(code:String, title:String, detail:String):ErrorMessage = {
    val fmt = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")
    ErrorMessage(code, title, detail, fmt.print(System.currentTimeMillis()))
  }
}
