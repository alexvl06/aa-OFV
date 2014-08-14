package co.com.alianza.app

import scala.concurrent.ExecutionContext
import spray.http.MediaType
import co.com.alianza.infrastructure.messages.MessageService
import akka.actor.ActorSelection
import spray.routing.Route
import akka.actor.Props
import co.com.alianza.domain.aggregates.web.ApiRequestCreator
import co.com.alianza.domain.aggregates.web.AnonymousActor
import co.com.alianza.util.json.Links_hal
import co.com.alianza.infrastructure.cache.UserCache
import scala.util.{ Success, Failure }
trait AlianzaCommons extends ApiRequestCreator with AlianzaActors {

  implicit val ec: ExecutionContext = MainActors.ex

  val mediaType = MediaType.custom("application/hal+json")

  //Definición del origin para crosssite
  //val origin = "alianza.transaccional.com"

  def setOptionsHeaders(): List[spray.http.HttpHeader] = {
    /* RawHeader("Access-Control-Allow-Origin", "*") ::
      RawHeader("Access-Control-Allow-Methods", "POST,GET") ::
      RawHeader("Access-Control-Allow-Credentials", "false") ::
      RawHeader("Access-Control-Allow-Headers", "Cookie,token, X-Requested-With, X-Prototype-Version, X-CSRF-Token, Content-Type,Expires,Cache-Control") ::
      */
    Nil
  }

  def requestExecute(message: MessageService, serviceActor: ActorSelection, cache: Boolean = false): Route =
    {
      ctx =>
        {
          if (cache) {
            UserCache.getUser(message) onComplete {
              case Success(result) =>
                if (!result.equals(""))
                  ctx.complete(result)
                else
                  apiRequest(ctx, Props(new AnonymousActor(serviceActor)), message)
              case Failure(t) => apiRequest(ctx, Props(new AnonymousActor(serviceActor)), message)
            }
          } else {
            apiRequest(ctx, Props(new AnonymousActor(serviceActor)), message)
          }

        }
    }

  def addHal(uri: String, links_hal: Option[List[Links_hal]], json: String): String = {
    val jsonResp = json.slice(1, json.length - 1)

    val links_halResp = new StringBuilder()

    links_hal match {
      case Some(links: List[Links_hal]) =>
        {
          for (link <- links) {
            val t1 = ", \n\t\t\"" + link.method + "\" : {\"href\": \"" + link.href + "\"}"
            links_halResp.append(t1)
          }
        }

      case _ =>
    }

    val links_halRespStr = links_halResp.toString()
    val comma = if (!jsonResp.isEmpty) ",\n" else "\n"
    val response = new StringBuilder()
    response append "{" append "\n"
    response append "\t\"_links\": {" append "\n"
    response append "\t\t\"self\": {\"href\": \"" append uri append "\"}" append links_halRespStr append "\n"
    response append "\t}" append comma
    response append jsonResp
    response append "}"
    response.toString()
  }

}

