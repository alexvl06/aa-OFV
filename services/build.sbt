import Common._
import Dependencies._


name := "auth-service"

def recaptcha4j = Seq(
  "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7"
)

Common.commonSettings

libraryDependencies ++= recaptcha4j




