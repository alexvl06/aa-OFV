package co.com.alianza.mail

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.{ FileResourceLoader, Resource }
import scala.io.Source

/**
 * @author smontanez
 */
trait MailTemplate {
  val engine = new TemplateEngine
  engine.resourceLoader = new FileResourceLoader {
    override def resource( uri: String ): Option[ Resource ] = {
      Some( Resource.fromSource( uri, Source.fromBytes( ( scalax.io.Resource.fromClasspath( uri ).byteArray ) ) ) )
    }
  }
}