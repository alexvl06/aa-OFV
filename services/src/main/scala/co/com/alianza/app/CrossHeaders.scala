package co.com.alianza.app

import spray.http._
import spray.http.HttpHeaders._
import spray.http.HttpMethods._

trait CrossHeaders {

  val domain: String = "http://fiduciaria.alianzaenlinea.com.co"

  def listCrossHeaders: List[spray.http.HttpHeader] = {
    val origins = `Access-Control-Allow-Origin`(SomeOrigins(Seq(HttpOrigin(domain))))
    val methods: `Access-Control-Allow-Methods` = `Access-Control-Allow-Methods`(GET, POST)
    val headers: `Access-Control-Allow-Headers` = `Access-Control-Allow-Headers`("Content-Type", "token")
    val csp = RawHeader("Content-Security-Policy", "default-src 'self'")
    val xpcd: RawHeader = RawHeader("X-Permitted-Cross-Domain-Policies", "master-only")
    val ns: RawHeader = RawHeader("X-Content-Type-Options", "nosniff")
    val xfo: RawHeader = RawHeader("X-Frame-Options", "DENY")
    val xss: RawHeader = RawHeader("X-XSS-Protection", "1")
    val strict: RawHeader = RawHeader("Strict-Transport-Security", "max-age=31536000")

    origins :: methods :: headers :: csp :: xpcd :: ns :: xfo :: xss :: strict :: Nil
  }

  //HTTP Strict Transport Security
  //Strict-Transport-Security: max-age=31536000;

  //Content-Security-Policy
  //X-Content-Type-Options con valor nosniff.
  //X-XSS-Protection con valor 1

}