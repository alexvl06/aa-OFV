import sbt._

object Dependencies {
  
	private[Dependencies] implicit class Exclude(module: ModuleID) {

		def kafkaExclusions: ModuleID = {
			module.logScalaExclude.excludeAll(
				ExclusionRule("com.sun.jmx" , "jmxri"),
				ExclusionRule("com.sun.jdmk", "jmxtools"),
				ExclusionRule("javax.jms"   , "jms"),
				ExclusionRule("jline"       , "jline")
			)
		}

		def jacksonExclude : ModuleID = {
			module.logScalaExclude.excludeAll(
				ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
        ExclusionRule("com.fasterxml.jackson.core", "jackson-core")
			)
		}

    def nettyExclude : ModuleID = module.logScalaExclude.exclude("io.netty","netty")

    def scalaParserCombinatorsExclusions: ModuleID = module.logScalaExclude.exclude("org.scala-lang.modules", "scala-parser-combinators_2.11")

    def scalaxmlExclude : ModuleID = module.logScalaExclude.exclude("org.scala.lang.modules","scala-xml_2.11")

    def slickExclude : ModuleID = module.jodaTimeExclusions.excludeAll(ExclusionRule("com.github.tminglei","slick-pg_core_2.11"))

    def logScalaExclude : ModuleID = module.logbackExclude.scalaLibraryExclude

    def logbackExclude: ModuleID = {
			module.log4jExclude.excludeAll(
				ExclusionRule("ch.qos.logback", "logback-classic"),
				ExclusionRule("ch.qos.logback", "logback-core")
			)
		}

    def jodaTimeExclusions : ModuleID = {
      module.logScalaExclude.excludeAll(
        ExclusionRule("joda-time","joda-time"),
        ExclusionRule("org-joda","joda-convert")
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

		val akkaActor 			= "com.typesafe.akka" 	    			 %% "akka-actor" 			 		 	% akkaVersion nettyExclude
		val akkaSlf4j 			= "com.typesafe.akka" 	    		 	 %% "akka-slf4j"       			% akkaVersion logScalaExclude
		val akkaCluster 		= "com.typesafe.akka"     			 	 % "akka-cluster_2.11"      % akkaClusterVersion nettyExclude
		val logbackClassic 	= "ch.qos.logback"     							% "logback-classic"  			% akkaLogbackVersion
		val scalaLibrary 		= "org.scala-lang" 					      	% "scala-library" 				% commonScalaVersion
		val scalaCompiler 	= "org.scala-lang" 					      	% "scala-compiler" 				% commonScalaVersion
		val scalaIOCore 		= "com.github.scala-incubator.io" 	%% "scala-io-core" 				% scalaioVersion logScalaExclude
		val scalaIOFile 		= "com.github.scala-incubator.io" 	%% "scala-io-file" 				% scalaioVersion logScalaExclude
		val scalazLib 			= "org.scalaz"				      			%%  "scalaz-core" 					% scalazVersion logScalaExclude
		val sprayCan 				= "io.spray"           		  			 %% "spray-can"    		  		% sprayVersion logScalaExclude
		val sprayRouting 		= "io.spray"           		  			 %% "spray-routing-shapeless2"  % sprayVersion logScalaExclude
		val sprayClient 		= "io.spray"           		  			 %% "spray-client"   				% sprayVersion logScalaExclude
		val sprayHttp 			= "io.spray"           		  			 %% "spray-http"    	  		% sprayVersion logScalaExclude
		val sprayHttpx 			= "io.spray"           		  			 %% "spray-httpx"   	  		% sprayVersion scalaxmlExclude
		val sprayJsonLib 		= "io.spray" 					      			 %% "spray-json" 		    		% sprayJsonVersion logScalaExclude
		val sprayCaching 		= "io.spray"           						 % "spray-caching"     		  % sprayVersion logScalaExclude

		val scalateLib		  = "org.fusesource.scalate"      		% "scalate-core_2.10" 		% Versions.scalateVersion
		val shapelessLib 			= "com.chuusai"				      			%% "shapeless" 		   		 	% shapelessVersion logScalaExclude

		val commonsLogging  = "commons-logging" 			  			 % "commons-logging" 	  		% apacheLoginVersion
		val commonsLang3Lib = "org.apache.commons" 		  			 % "commons-lang3" 	    		% apacheLangVersion
		val axisLib 						= "org.apache.axis" 			  			 % "axis" 	    							% apacheAxisVersion
		val wss4j 					= "org.apache.ws.security" 				 % "wss4j" 									% wss4jVersion

		val commonsCodecLib = "commons-codec" 			    			 % "commons-codec" 	    		% apacheCodecVersion
		val discovery 			= "commons-discovery" 						 % "commons-discovery" 			%	commonsDiscovery
		val wsdl4j					= "wsdl4j"												 % "wsdl4j"									%	wsdl4jVersion
		val jaxrpc 					= "javax.xml"               			 % "jaxrpc-api"          		% jaxrpcVersion

		val ojdbc 					= "oracle"					        			 % "ojdbc"				      		% ojdbcVersion

		val playJsonLib 					= "com.typesafe.play" 		       	%% "play-json" 			    	% playJsonVersion jodaTimeExclusions
		val jacksonDatabindLib 		= "com.fasterxml.jackson.core"    % "jackson-databind" 		  % jacksonDataBindVersion
		val jacksonModuleScalaLib = "com.fasterxml.jackson.module" 	%% "jackson-module-scala" 	% jacksonModuleScalaVersion jacksonExclude
		val jsonToken 						= "com.googlecode.jsontoken" 			% "jsontoken" 							% jsonTokenVersion jodaTimeExclusions
		val ninbus 								= "com.nimbusds"									% "nimbus-jose-jwt" 				% ninbusVersion
		val jasyptLib 						= "org.jasypt" 										% "jasypt" 									% jasyptVersion
		val kafkaLib 							= "org.apache.kafka" 							% "kafka_2.10" 							% kafkaVersion kafkaExclusions

		val slickLib 							= "com.typesafe.slick" 						 %% "slick"                   % slickVersion log4jExclude
		val postgresqlLib 				= "postgresql"          					 % "postgresql"              % postgreSqlVersion log4jExclude
		val c3p0Lib 							= "c3p0"                					 % "c3p0"                    % c3p0Version log4jExclude
		val slickPGLib 						= "com.github.tminglei" 					 %% "slick-pg"           % slickpgVersion slickExclude
		val slickPG_jodaTimeLib 	= "com.github.tminglei" 					 %% "slick-pg_joda-time" % slickpgJodaTimeVersion slickExclude
		val recaptcha4j 					= "net.tanesha.recaptcha4j" 			 % "recaptcha4j" 						% "0.0.7"
	}

	private[Dependencies] object Test {
		import Versions._

		val akkaTestkit 		= "com.typesafe.akka"       %%  "akka-testkit"        % akkaVersion 				% "test" logScalaExclude
		val junitLib 				= "junit"                   %  "junit"                % junitVersion    		% "test"
		val restAssuredLib 	= "com.jayway.restassured"  %  "rest-assured"         % restAssuredVersion  % "test"
		val scalatestLib 		= "org.scalatest"           %%  "scalatest"      			% scalatestVersion   	% "test" logScalaExclude
		val scalacheckLib 	= "org.scalacheck"          %% "scalacheck"           % scalacheckVersion  	% "test" logScalaExclude
		val sprayTestkitLib = "io.spray" 			          %%  "spray-testkit" 	    % sprayTestkitVersion % "test" logScalaExclude
		val specs2Lib 			= "org.specs2"              %% "specs2"               % specs2Version  			% "test" logScalaExclude
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
