import sbt._
import Keys._

object Common {

	autoScalaLibrary := false

	lazy val akkaVersion = "2.3.0"
	lazy val sprayVersion = "1.3.1"

	/**
	 * Scala version
	 **/
	def commonScalaVersion = "2.10.3"

	/**
	 * scalac arguments
	 **/
	def commonScalacOptions = Seq(
		"-unchecked",
		"-deprecation",
		"-Xlint",
		"-Ywarn-dead-code",
		"-language:_",
		"-target:jvm-1.7",
		"-encoding", "UTF-8"
	)

	/**
	 * Maven repositories
	 **/
	def commonResolvers = Seq(
		"Sonatype Snapshots"  at "http://oss.sonatype.org/content/repositories/snapshots",
		"Sonatype Releases"   at "http://oss.sonatype.org/content/repositories/releases",
		"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
		"Spray.io repository" at "http://repo.spray.io",
		"artifactory alianza"    at "http://172.20.0.67:8081/artifactory/third-party-libs/",
		"maven repository"    at "http://repo1.maven.org/maven2/"
	)

	/**
	 * Scala basic libraries
	 **/
	def commonScalaLibraries = Seq (
		"org.scala-lang" 					         % "scala-library" 	% "2.10.3",
		"org.scala-lang" 					         % "scala-compiler" % "2.10.3",
		"com.github.scala-incubator.io" 	%% "scala-io-core" 	% "0.4.2",
		"com.github.scala-incubator.io" 	%% "scala-io-file" 	% "0.4.2" 

	)

	def functionalProgrammingLibraries = Seq(
		"org.scalaz"				      %%  "scalaz-core" 			  % "7.1.0-M7"
	)

	def akkaLibraries = Seq(
		"com.typesafe.akka" 	    %%  "akka-actor" 			    % akkaVersion withSources(),
	  "com.typesafe.akka" 	    %%  "akka-slf4j"       		% akkaVersion withSources(),
    "com.typesafe.akka"       %%  "akka-testkit"        % akkaVersion withSources(),
	 	"ch.qos.logback"     		  %   "logback-classic"  		% "1.0.13" withSources()
  )

  def sprayLibraries = Seq(
	  "io.spray"           		  % 	"spray-can"    		    % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-routing"  		  % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-client"   		  % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-http"    	    % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-httpx"   	    % sprayVersion withSources(),
		"io.spray" 					      %% 	"spray-json" 		      % "1.2.5" withSources(),
		"com.chuusai"				      %% 	"shapeless" 		      % "1.2.4" withSources()
  )

  def testingLibraries = Seq(
    "junit"                   %  "junit"                % "4.10"    % "test",
    "com.jayway.restassured"  %  "rest-assured"         % "1.8.1"   % "test",
    "org.scalatest"           %  "scalatest_2.10"       % "1.9.1"   % "test",
    "org.scalacheck"          %% "scalacheck"           % "1.11.3"  % "test",
    "io.spray" 			          %  "spray-testkit" 	      % "1.3.1"   % "test",
    "org.specs2"              %% "specs2"               % "2.3.12-scalaz-7.1.0-M6"  % "test"
  )

	def apacheCommonsLibraries = Seq( 
		"commons-logging" 			  % 	"commons-logging" 	  % "1.1.3",
		"org.apache.commons" 		  % 	"commons-lang3" 	    % "3.1",
		"commons-codec" 			    % 	"commons-codec" 	    % "1.8",
		"org.apache.axis" 			    % 	"axis" 	    % "1.4",
		"wsdl4j"			%	"wsdl4j"	%	"1.4",
		"commons-discovery" 	%	 "commons-discovery" 	% 	"0.2",
    "org.apache.ws.security" % "wss4j" % "1.6.16",
  "javax.xml"               %   "jaxrpc-api"          % "1.1"
	)

	def oracle = Seq( 
		"oracle"					        %	  "ojdbc"				          % "6"
	)

	def json = Seq(
    "com.typesafe.play" 		       %% 	"play-json" 			    % "2.2.0"   withSources(),
		"com.fasterxml.jackson.core"    % "jackson-databind" 		  % "2.2.2",
		"com.fasterxml.jackson.module" %% "jackson-module-scala"  % "2.2.2"
	)

  def jsonWebTokenLibraries = Seq(
    "com.googlecode.jsontoken" % "jsontoken" % "1.1"
  )

  /*def scalaEnumerations = Seq(
    "se.radley" %% "play-plugins-enumeration" % "1.1.0"
  )*/

  def commonLibraries   =  testingLibraries ++ commonScalaLibraries ++ apacheCommonsLibraries ++ json ++ functionalProgrammingLibraries
 	def reactiveLibraries =   akkaLibraries ++ sprayLibraries ++ oracle ++ jsonWebTokenLibraries

}
