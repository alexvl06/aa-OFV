import Common._

Common.commonSettings

lazy val recaptcha4j = Seq("net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7")

libraryDependencies ++= recaptcha4j

name := "auth-service"

mainClass in Revolver.reStart := Option("co.com.alianza.app.Boot")
