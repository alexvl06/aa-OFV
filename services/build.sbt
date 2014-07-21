import AssemblyKeys._
import Common._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case _ => MergeStrategy.discard
    }
  case x => MergeStrategy.first
}
}

test in assembly := {}

outputPath in assembly := file( "../alianza-autenticacion-autorizacion-service.jar" )

name := "autenticacion-autorizacion-service"

organization := "co.s4n"

version := "0.1-SNAPSHOT"

scalaVersion := commonScalaVersion

scalacOptions ++= commonScalacOptions

unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil

unmanagedSourceDirectories in Test := (scalaSource  in Test).value :: Nil

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

resolvers ++= commonResolvers

def recaptcha4j = Seq(
  "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7"
)

libraryDependencies ++= commonLibraries ++ reactiveLibraries ++ recaptcha4j

seq( Revolver.settings: _* )

mainClass in Revolver.reStart := Some( "co.s4n.template.Boot" )
