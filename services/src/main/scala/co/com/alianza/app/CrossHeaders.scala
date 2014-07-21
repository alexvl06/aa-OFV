package co.com.alianza.app

/**
 *
 * @author seven4n
 */
trait  CrossHeaders {

  def listCrossHeaders: List[ spray.http.HttpHeader ] = {
    import spray.http.HttpHeaders._
    import spray.http._
    import spray.http.HttpMethods._

    List(
      `Access-Control-Allow-Methods`( OPTIONS, GET, POST, PUT ),
      `Access-Control-Allow-Headers`( "Accept", "Authorization", "Content-Type", "X-Requested-With" ),
      `Access-Control-Expose-Headers`( "Accept", "WWW-Authenticate" ),
      `Access-Control-Allow-Origin`( AllOrigins ) )
  }
}
