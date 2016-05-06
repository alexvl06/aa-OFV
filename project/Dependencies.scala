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

    //def nettyExclude : ModuleID = module.logScalaExclude.exclude("io.netty","netty")

    def scalaParserCombinatorsExclusions: ModuleID = module.logScalaExclude.exclude("org.scala-lang.modules", "scala-parser-combinators_2.11")

    def scalaxmlExclude : ModuleID = module.logScalaExclude.exclude("org.scala.lang.modules","scala-xml_2.11")

    def slickExclude : ModuleID = module.jodaTimeExclusions.excludeAll(ExclusionRule("com.github.tminglei","slick-pg_core_2.11"))

    def jodaTimeExclusions : ModuleID = {
      module.logScalaExclude.excludeAll(
        ExclusionRule("joda-time","joda-time"),
        ExclusionRule("org-joda","joda-convert")
      )
    }
    
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

    val scalaLibrary 		= "org.scala-lang" 					        % "scala-library" 				% commonScalaVersion
    val scalaCompiler 	= "org.scala-lang" 					      	% "scala-compiler" 				% commonScalaVersion
    
    val akkaActor 			= "com.typesafe.akka" 	    			  %% "akka-actor" 			 		 	% akka //nettyExclude
		val akkaSlf4j 			= "com.typesafe.akka" 	    		 	  %% "akka-slf4j"       			% akka logScalaExclude
		val akkaClusterLib 	= "com.typesafe.akka"     			 	 % "akka-cluster_2.11"       % akkaCluster //nettyExclude

    val sprayCan 				= "io.spray"           		  			  %% "spray-can"    		  		  % spray logScalaExclude
    val sprayRouting 		= "io.spray"           		  			  %% "spray-routing-shapeless2" % spray logScalaExclude
    val sprayClient 		= "io.spray"           		  			  %% "spray-client"   				  % spray logScalaExclude
    val sprayHttp 			= "io.spray"           		  			  %% "spray-http"    	  		    % spray logScalaExclude
    val sprayHttpx 			= "io.spray"           		  			  %% "spray-httpx"   	  		    % spray scalaxmlExclude
    val sprayJsonLib 		= "io.spray" 					      			  %% "spray-json" 		    		  % sprayJson logScalaExclude
    val sprayCaching 		= "io.spray"           						  % "spray-caching"     		    % spray logScalaExclude

    val commonsLogging  = "commons-logging" 			  			  % "commons-logging" 	  		% apacheLogin 
    val logbackClassic 	= "ch.qos.logback"     						  % "logback-classic"  			  % akkaLogback 
    val commonsLang3Lib = "org.apache.commons" 		  			  % "commons-lang3" 	    		% apacheLang 
    val axisLib 				= "org.apache.axis"      			  	  % "axis" 	    							% apacheAxis 
    val wss4jLib 				= "org.apache.ws.security" 				  % "wss4j" 									% wss4j 
		
		val scalaIOCoreLib 		= "com.github.scala-incubator.io"  %% "scala-io-core" 				% scalaio logScalaExclude
		val scalaIOFileLib 		= "com.github.scala-incubator.io"  %% "scala-io-file" 				% scalaio logScalaExclude
		val scalazLib 	  		= "org.scalaz"				      			 %%  "scalaz-core" 					% scalaz logScalaExclude
		val scalateLib		    = "org.fusesource.scalate"      	  % "scalate-core_2.10" 		% scalate 
		val shapelessLib 			= "com.chuusai"				      		   %% "shapeless" 		   		 	% shapeless logScalaExclude
    
		val commonsCodecLib = "commons-codec" 			    			  % "commons-codec" 	    		% apacheCodec 
		val discoveryLib 		= "commons-discovery" 						  % "commons-discovery" 			%	commonsDiscovery
		val wsdl4jLib				= "wsdl4j"												  % "wsdl4j"									%	wsdl4j
		val jaxrpcLib 			= "javax.xml"               			  % "jaxrpc-api"          		% jaxrpc

		val ojdbcLib 				= "oracle"					        			 	% "ojdbc"				      		% ojdbc

		val playJsonLib 					= "com.typesafe.play" 		       	%% "play-json" 			    	  % playJson
		val jacksonDatabindLib 		= "com.fasterxml.jackson.core"    % "jackson-databind" 		    % jacksonDataBind 
		val jacksonModuleScalaLib = "com.fasterxml.jackson.module" 	%% "jackson-module-scala" 	% jacksonModuleScala jacksonExclude
		val jsonTokenLib 					= "com.googlecode.jsontoken" 			% "jsontoken" 							% jsonToken
		val ninbusLib 						= "com.nimbusds"									% "nimbus-jose-jwt" 				% ninbus
		val jasyptLib 						= "org.jasypt" 										% "jasypt" 									% jasypt 
		val kafkaLib 							= "org.apache.kafka" 							% "kafka_2.10" 							% kafka kafkaExclusions

		val slickLib 							= "com.typesafe.slick" 						 %% "slick"                 % slick log4jExclude
		val postgresqlLib 				= "postgresql"          					 % "postgresql"             % postgreSql log4jExclude
		val c3p0Lib 							= "c3p0"                					 % "c3p0"                   % c3p0 log4jExclude
		val slickPGLib 						= "com.github.tminglei" 					 %% "slick-pg"              % slickpg slickExclude
		val slickPG_jodaTimeLib 	= "com.github.tminglei" 					 %% "slick-pg_joda-time"    % slickpgJodaTime slickExclude
		val recaptcha4jLib				= "net.tanesha.recaptcha4j" 			 % "recaptcha4j" 						% recaptcha4j
	}

	private[Dependencies] object Test {
		import Versions._

		val akkaTestkit 		= "com.typesafe.akka"       %% "akka-testkit"       % akka 				% "test" logScalaExclude
		val junitLib 				= "junit"                   %  "junit"              % junit    		% "test"
		val restAssuredLib 	= "com.jayway.restassured"  %  "rest-assured"       % restAssured  % "test"
		val scalatestLib 		= "org.scalatest"           %% "scalatest"      		% scalatest   	% "test" logScalaExclude
		val scalacheckLib 	= "org.scalacheck"          %% "scalacheck"         % scalacheck  	% "test" logScalaExclude
		val sprayTestkitLib = "io.spray" 			          %% "spray-testkit" 	    % sprayTestkit % "test" logScalaExclude
		val specs2Lib 			= "org.specs2"              %% "specs2"             % specs2  			% "test" logScalaExclude
	}

	import Dependencies.Compile._
	import Dependencies.Test._

	val scalaLibs: Seq[ModuleID]             = Seq(scalaCompiler, scalaLibrary)
	val akkaLibs: Seq[ModuleID]              = Seq(akkaActor, akkaClusterLib, akkaSlf4j, logbackClassic)
	val sprayLibs: Seq[ModuleID]             = Seq(sprayCan, sprayRouting, sprayClient, sprayHttp, sprayHttpx, sprayJsonLib)
	val kafkaLibs: Seq[ModuleID]             = Seq(kafkaLib)
	val functionalLibs: Seq[ModuleID]        = Seq(scalaIOCoreLib, scalaIOFileLib, scalazLib, shapelessLib)

	val slickLibs : Seq[ModuleID]						 = Seq(slickLib , slickPGLib, slickPG_jodaTimeLib)
	val utilLibs: Seq[ModuleID]              = Seq(
		commonsLang3Lib, commonsCodecLib, playJsonLib, jacksonDatabindLib, jacksonModuleScalaLib, jasyptLib, scalateLib, axisLib, jaxrpcLib, wss4jLib, ninbusLib, jsonTokenLib
	)
  
  val modulePersistenceLibs: Seq[ModuleID] = Seq(postgresqlLib , c3p0Lib, ojdbcLib) ++ slickLibs
	val moduleService: Seq [ModuleID] = Seq(recaptcha4jLib)
	val testLibs: Seq[ModuleID] = Seq(akkaTestkit, sprayTestkitLib, scalatestLib, junitLib, restAssuredLib, scalacheckLib, specs2Lib)
}
