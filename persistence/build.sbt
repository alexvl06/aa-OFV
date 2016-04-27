import Common._
import Versions._

name := "auth-persistence"

def dbLibs = Seq(
  "org.postgresql"      % "postgresql"              % Versions.postgreSqlVersion2,
  "com.typesafe.slick" %% "slick"                   % Versions.slickVersion,
  "postgresql"          % "postgresql"              % Versions.postgreSqlVersion,
  "c3p0"                % "c3p0"                    % Versions.c3p0Version,
  "com.github.tminglei" % "slick-pg_2.10"           % Versions.slickpgVersion,
  "com.github.tminglei" % "slick-pg_joda-time_2.10" % Versions.slickpgVersion
)

Common.commonSettings

libraryDependencies ++= dbLibs

