libraryDependencies ++=
  Seq(
    "io.spray" %% "spray-json" % "1.3.5",
    "org.parboiled" %% "parboiled-scala" % "1.3.1" % "compile",
    "org.specs2" %% "specs2-core" % "4.6.0" % "test"
  )

initialCommands in console += """
    import spray.json._
    import DefaultJsonProtocol._
    import lenses._
    import JsonLenses._
"""

scalaVersion := "2.13.0"

crossScalaVersions := Seq("2.13.0", "2.12.8", "2.11.12")
