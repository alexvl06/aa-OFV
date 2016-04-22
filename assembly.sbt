import sbt.Keys._
import sbtassembly.Plugin.AssemblyKeys._

// ---------------------
// Settings
// ---------------------

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case "reference.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("mailcap" :: Nil) =>
        MergeStrategy.first
      case _ => MergeStrategy.discard
    }
  case x => MergeStrategy.first
} }

test in assembly := {}

outputPath in assembly := file(s"../${name.value}_${scalaBinaryVersion.value}-${(version in ThisBuild).value}.jar")

mainClass in Revolver.reStart := Option("co.com.alianza.app.Boot")

// -----------------------
// Custom settings
// -----------------------
