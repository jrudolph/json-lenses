publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

pomExtra :=
  Helpers.generatePomExtra("git@github.com:jrudolph/json-lenses.git",
                           "scm:git:git@github.com:jrudolph/json-lenses.git",
                           "jrudolph", "Johannes Rudolph")

useGpg := true

site.settings

site.includeScaladoc()

ghpages.settings

git.remoteRepo := "git@github.com:jrudolph/json-lenses.git"

ScalariformSupport.formatSettings
