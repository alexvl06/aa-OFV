import sbt._

object Dependencies {

	/**
	 * Scala basic libraries
	 **/

	private[Dependencies] implicit class Exclude(module: ModuleID) {

		def kafkaExclusions: ModuleID = {
			module.logScalaExclude.excludeAll(
				ExclusionRule("com.sun.jmx" , "jmxri"),
				ExclusionRule("com.sun.jdmk", "jmxtools"),
				ExclusionRule("javax.jms"   , "jms"),
				ExclusionRule("jline"       , "jline")
			)
		}

		def scalaParserCombinatorsExclude: ModuleID = module.logScalaExclude.exclude("org.scala-lang.modules", "scala-parser-combinators_2.11")

		//def scalaReflectExclude: ModuleID = module..exclude("org.scala-lang", "scala-reflect")

		def logScalaExclude : ModuleID = module.logbackExclude.scalaLibraryExclude

		def logbackExclude: ModuleID = {
			module.log4jExclude.excludeAll(
				ExclusionRule("ch.qos.logback", "logback-classic"),
				ExclusionRule("ch.qos.logback", "logback-core")
			)
		}

		def log4jExclude: ModuleID = {
			module.excludeAll(
				ExclusionRule("commons-logging", "commons-logging"),
				ExclusionRule("log4j"          , "log4j"),
				ExclusionRule("org.slf4j"      , "jcl-over-slf4j"),
				ExclusionRule("org.slf4j"      , "jul-to-slf4j"),
				ExclusionRule("org.slf4j"      , "log4j-over-slf4j"),
				ExclusionRule("org.slf4j"      , "slf4j-api"),
				ExclusionRule("org.slf4j"      , "slf4j-jcl"),
				ExclusionRule("org.slf4j"      , "slf4j-jdk14"),
				ExclusionRule("org.slf4j"      , "slf4j-log4j12"),
				ExclusionRule("org.slf4j"      , "slf4j-nop"),
				ExclusionRule("org.slf4j"      , "slf4j-simple")
			)
		}

		def scalaLibraryExclude: ModuleID = module.exclude("org.scala-lang", "scala-library")
	}


	private[Dependencies] object Compile {
		import Versions._

		val akkaActor 			= "com.typesafe.akka" 	    			 %% "akka-actor" 			 		 	% akkaVersion logScalaExclude
		val akkaSlf4j 			= "com.typesafe.akka" 	    		 	 %% "akka-slf4j"       			% akkaVersion
		val akkaCluster 		= "com.typesafe.akka"     			 	 % "akka-cluster_2.11"      		% akkaClusterVersion
		val logbackClassic 	= "ch.qos.logback"     							% "logback-classic"  				% akkaLogbackVersion
		val scalaLibrary 		= "org.scala-lang" 					      	% "scala-library" 					% commonScalaVersion
		val scalaCompiler 	= "org.scala-lang" 					      	% "scala-compiler" 					% commonScalaVersion
		val scalaIOCore 		= "com.github.scala-incubator.io" 	%% "scala-io-core" 					% scalaioVersion logScalaExclude
		val scalaIOFile 		= "com.github.scala-incubator.io" 	%% "scala-io-file" 					% scalaioVersion logScalaExclude
		val scalazLib 			= "org.scalaz"				      			%%  "scalaz-core" 					% scalazVersion
		val sprayCan 				= "io.spray"           		  			 %% "spray-can"    		  		% sprayVersion
		val sprayRouting 		= "io.spray"           		  			 %% "spray-routing-shapeless2"  % sprayVersion
		val sprayClient 		= "io.spray"           		  			 %% "spray-client"   				% sprayVersion
		val sprayHttp 			= "io.spray"           		  			 %% "spray-http"    	  		% sprayVersion
		val sprayHttpx 			= "io.spray"           		  			 %% "spray-httpx"   	  		% sprayVersion
		val sprayJsonLib 		= "io.spray" 					      			 %% "spray-json" 		    		% sprayJsonVersion logScalaExclude
		val sprayCaching 		= "io.spray"           						 % "spray-caching"     		  % sprayVersion

		val scalateLib		  = "org.fusesource.scalate"      		% "scalate-core_2.10" 		% Versions.scalateVersion
		val shapelessLib 			= "com.chuusai"				      			%% "shapeless" 		   		 	% shapelessVersion

		val commonsLogging  = "commons-logging" 			  			 % "commons-logging" 	  		% apacheLoginVersion
		val commonsLang3Lib = "org.apache.commons" 		  			 % "commons-lang3" 	    		% apacheLangVersion
		val axisLib 						= "org.apache.axis" 			  			 % "axis" 	    							% apacheAxisVersion
		val wss4j 					= "org.apache.ws.security" 				 % "wss4j" 									% wss4jVersion

		val commonsCodecLib = "commons-codec" 			    			 % "commons-codec" 	    		% apacheCodecVersion
		val discovery 			= "commons-discovery" 						 % "commons-discovery" 			%	commonsDiscovery
		val wsdl4j					= "wsdl4j"												 % "wsdl4j"									%	wsdl4jVersion
		val jaxrpc 					= "javax.xml"               			 % "jaxrpc-api"          		% jaxrpcVersion

		val ojdbc 					= "oracle"					        			 % "ojdbc"				      		% ojdbcVersion

		val playJsonLib 					= "com.typesafe.play" 		       	%% "play-json" 			    	% playJsonVersion
		val jacksonDatabindLib 		= "com.fasterxml.jackson.core"    % "jackson-databind" 		  % jacksonDataBindVersion
		val jacksonModuleScalaLib = "com.fasterxml.jackson.module" 	%% "jackson-module-scala" 	% jacksonModuleScalaVersion
		val jsonToken 						= "com.googlecode.jsontoken" 			% "jsontoken" 							% jsonTokenVersion
		val ninbus 								= "com.nimbusds"									% "nimbus-jose-jwt" 				% ninbusVersion
		val jasyptLib 						= "org.jasypt" 										% "jasypt" 									% jasyptVersion
		val kafkaLib 							= "org.apache.kafka" 							% "kafka_2.10" 							% kafkaVersion kafkaExclusions

		val slickLib 							= "com.typesafe.slick" 						 %% "slick"                   % slickVersion log4jExclude
		val postgresqlLib 				= "postgresql"          					 % "postgresql"              % postgreSqlVersion log4jExclude
		val c3p0Lib 							= "c3p0"                					 % "c3p0"                    % c3p0Version log4jExclude
		val slickPGLib 						= "com.github.tminglei" 					 %% "slick-pg"           % slickpgVersion log4jExclude
		val slickPG_jodaTimeLib 	= "com.github.tminglei" 					 %% "slick-pg_joda-time" % slickpgJodaTimeVersion log4jExclude
		val recaptcha4j 					= "net.tanesha.recaptcha4j" 			 % "recaptcha4j" 						% "0.0.7"
	}

	private[Dependencies] object Test {
		import Versions._

		val akkaTestkit 		= "com.typesafe.akka"       %%  "akka-testkit"        % akkaVersion 				% "test"
		val junitLib 				= "junit"                   %  "junit"                % junitVersion    		% "test"
		val restAssuredLib 	= "com.jayway.restassured"  %  "rest-assured"         % restAssuredVersion  % "test"
		val scalatestLib 		= "org.scalatest"           %%  "scalatest"      			% scalatestVersion   	% "test"
		val scalacheckLib 	= "org.scalacheck"          %% "scalacheck"           % scalacheckVersion  	% "test"
		val sprayTestkitLib = "io.spray" 			          %%  "spray-testkit" 	      % sprayTestkitVersion % "test"
		val specs2Lib 			= "org.specs2"              %% "specs2"               % specs2Version  			% "test"
	}

	import Dependencies.Compile._
	import Dependencies.Test._

	val scalaLibs: Seq[ModuleID]             = Seq(scalaCompiler, scalaLibrary)
	val akkaLibs: Seq[ModuleID]              = Seq(akkaActor, akkaCluster, akkaSlf4j, logbackClassic)
	val sprayLibs: Seq[ModuleID]             = Seq(sprayCan, sprayRouting, sprayClient, sprayHttp, sprayHttpx, sprayJsonLib)
	val kafkaLibs: Seq[ModuleID]             = Seq(kafkaLib)
	val functionalLibs: Seq[ModuleID]        = Seq(scalaIOCore, scalaIOFile, scalazLib, shapelessLib)

	val utilLibs: Seq[ModuleID]              = Seq(
		commonsLang3Lib, commonsCodecLib, playJsonLib, jacksonDatabindLib, jacksonModuleScalaLib, jasyptLib
	)

	val moduleCommonLibs: Seq[ModuleID]      = Seq(scalateLib, axisLib, jaxrpc, wss4j)
	val modulePersistenceLibs: Seq[ModuleID] = Seq(slickLib, postgresqlLib , c3p0Lib , slickPGLib, slickPG_jodaTimeLib, ojdbc)
	val moduleService: Seq [ModuleID] = Seq(recaptcha4j, ninbus, jsonToken)
	val testLibs: Seq[ModuleID] = Seq(akkaTestkit, sprayTestkitLib, scalatestLib, junitLib, restAssuredLib, scalacheckLib, specs2Lib)
}
