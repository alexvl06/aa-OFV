// ---------------------
// Settings
// ---------------------
publishMavenStyle := true

pomIncludeRepository := { _ => false }

publishArtifact in Test := false

publishTo := {
  val nexus = "http://nexus.seven4n.com/content/repositories/"
  if (version.value.trim.endsWith("SNAPSHOT")) {
    Option("S4N Nexus Snapshots Publish TO" at nexus.concat("alianza-portal-transaccional-binaries-snapshots"))
  } else {
    Option("S4N Nexus Releases Publish TO" at nexus.concat("alianza-portal-transaccional-binaries"))
  }
}


//credentials += Credentials(Path.userHome / ".alianza" / ".credentials")
