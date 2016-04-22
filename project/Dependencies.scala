import sbt._
import Keys._
import Versions._

object Dependencies {

	/**
	 * Scala basic libraries
	 **/
	private val commonScalaLibraries = Seq (
		"org.scala-lang" 					         % "scala-library" 	% commonScalaVersion,
		"org.scala-lang" 					         % "scala-compiler" % commonScalaVersion,
		"com.github.scala-incubator.io" 	%% "scala-io-core" 	% scalaioVersion,
		"com.github.scala-incubator.io" 	%% "scala-io-file" 	% scalaioVersion

	)

	private val functionalProgrammingLibraries = Seq(
		"org.scalaz"				      %%  "scalaz-core" 			  % scalazVersion
	)

	private val akkaLibraries = Seq(
		"com.typesafe.akka" 	    %%  "akka-actor" 			    % akkaVersion withSources(),
	  "com.typesafe.akka" 	    %%  "akka-slf4j"       		% akkaVersion withSources(),
    "com.typesafe.akka"       %%  "akka-testkit"        % akkaVersion withSources(),
    "com.typesafe.akka"       %%  "akka-cluster"        % akkaVersion withSources(),
	 	"ch.qos.logback"     		  %   "logback-classic"  		% akkaLogbackVersion withSources()
  )

	private val sprayLibraries = Seq(
	  "io.spray"           		  % 	"spray-can"    		    % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-routing"  		  % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-client"   		  % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-http"    	    % sprayVersion withSources(),
	  "io.spray"           		  % 	"spray-httpx"   	    % sprayVersion withSources(),
		"io.spray" 					      %% 	"spray-json" 		      % sprayJsonVersion withSources(),
		"io.spray"           			% 	"spray-caching"       % sprayVersion withSources(),
		"com.chuusai"				      %% 	"shapeless" 		      % shapelessVersion withSources()
  )

	private val testingLibraries = Seq(
    "junit"                   %  "junit"                % junitVersion    % "test",
    "com.jayway.restassured"  %  "rest-assured"         % restAssuredVersion   % "test",
    "org.scalatest"           %  "scalatest_2.10"       % scalatestVersion   % "test",
    "org.scalacheck"          %% "scalacheck"           % scalacheckVersion  % "test",
    "io.spray" 			          %  "spray-testkit" 	      % sprayTestkitVersion   % "test",
    "org.specs2"              %% "specs2"               % specs2Version  % "test"
  )

	private val apacheCommonsLibraries = Seq(
		"commons-logging" 			  % "commons-logging" 	  % apacheLoginVersion,
		"org.apache.commons" 		  % "commons-lang3" 	    % apacheLangVersion,
		"commons-codec" 			    % "commons-codec" 	    % apacheCodecVersion,
		"org.apache.axis" 			  % "axis" 	    					% apacheAxisVersion,
		"wsdl4j"									%	"wsdl4j"							%	wsdl4jVersion,
		"commons-discovery" 			%	"commons-discovery" 	%	commonsDiscovery,
    "org.apache.ws.security" 	% "wss4j" 							% wss4jVersion,
  	"javax.xml"               % "jaxrpc-api"          % jaxrpcVersion
	)

	private val oracle = Seq(
		"oracle"					        %	  "ojdbc"				          % ojdbcVersion
	)

	private val json = Seq(
    "com.typesafe.play" 		       	%% 	"play-json" 			    	% playVersion   withSources(),
		"com.fasterxml.jackson.core"    % 	"jackson-databind" 		  % jacksonDataBindVersion,
		"com.fasterxml.jackson.module" 	%% 	"jackson-module-scala" 	% jacksonDataBindVersion
	)

	private val jsonWebTokenLibraries = Seq(
    "com.googlecode.jsontoken" 		% "jsontoken" 			% jsonTokenVersion,
    "com.nimbusds"								% "nimbus-jose-jwt" % ninbusVersion
  )

	private val jasyptLibraries= Seq(
    "org.jasypt" % "jasypt" % jasyptVersion
  )

	private val kafka = Seq(
		"org.apache.kafka" % "kafka_2.10" % kafkaVersion exclude("javax.jms" , "jms") exclude("com.sun.jdmk" , "jmxtools") exclude("com.sun.jmx" , "jmxri")
	)

	val commonLibraries : Seq[ModuleID]   =  testingLibraries ++ commonScalaLibraries ++ apacheCommonsLibraries ++ json ++ functionalProgrammingLibraries ++ jasyptLibraries ++ kafka
	val reactiveLibraries : Seq[ModuleID]  =   akkaLibraries ++ sprayLibraries ++ oracle ++ jsonWebTokenLibraries
}
