package co.com.alianza.infrastructure

import spray.routing.Rejection
import scala.concurrent.Future
import spray.routing.RequestContext
import spray.routing.Directive
import shapeless.{ HNil, :: }
import co.com.alianza.infrastructure.messages.MessageService

package object cache {

  type Cache[T] = Either[T, T]
  type ContextCache[T] = RequestContext => Future[Cache[T]]
  type DirectiveCache[T] = Directive[T :: HNil]
  type Directive0 = Directive[HNil]

}