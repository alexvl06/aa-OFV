import Common._
import Versions._

name := "auth-common"

def dbLibs = Seq( "org.fusesource.scalate"      % "scalate-core_2.10" % Versions.scalateVersion  )

Common.commonSettings

libraryDependencies ++= dbLibs
