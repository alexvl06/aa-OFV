import Common._

name := "fiduciaria-alianza-aa-common"

organization := "co.s4n"

version := "0.1-SNAPSHOT"

scalaVersion := commonScalaVersion

scalacOptions ++= commonScalacOptions

//unmanagedSourceDirectories in Compile := (scalaSource in Compile).value :: Nil

//unmanagedSourceDirectories in Test := (scalaSource  in Test).value :: Nil

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.projectFlavor := EclipseProjectFlavor.Scala

resolvers ++= commonResolvers

def scalate = Seq(
  "org.fusesource.scalate" % "scalate-core_2.10" % "1.6.1"
)

libraryDependencies ++= commonLibraries ++ reactiveLibraries ++ scalate

//seq( Revolver.settings: _* )

//baseDirectory in Revolver.reStart := file("./")
