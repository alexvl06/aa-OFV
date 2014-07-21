import sbt._
import Keys._

object Build extends Build {
	lazy val autenticacionAutorizacionCommon  = Project( "autenticacion-autorizacion-common", file( "common" ) )
	lazy val autenticacionAutorizacionPersistence = Project( "autenticacion-autorizacion-persistence", file( "persistence" ) ).dependsOn( autenticacionAutorizacionCommon )
	lazy val autenticacionAutorizacionService = Project( "alianza-autenticacion-autorizacion", file( "services" ) ).dependsOn( autenticacionAutorizacionCommon ).dependsOn( autenticacionAutorizacionPersistence )
}
