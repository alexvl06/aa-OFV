import sbt._
import Keys._

object Build extends Build {
	lazy val autenticacionAutorizacionCommon  = Project( "fiduciaria-alianza-aa-common", file( "common" ) )
	lazy val autenticacionAutorizacionPersistence = Project( "fiduciaria-alianza-aa-persistence", file( "persistence" ) ).dependsOn( autenticacionAutorizacionCommon )
	lazy val autenticacionAutorizacionService = Project( "fiduciaria-alianza-aa-service", file( "services" ) ).dependsOn( autenticacionAutorizacionCommon % "compile->compile;test->test" ).dependsOn( autenticacionAutorizacionPersistence % "compile->compile;test->test" )
}
