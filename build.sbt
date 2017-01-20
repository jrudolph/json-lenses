scalaVersion := "2.12.1"

libraryDependencies ++=
  Seq(
    "io.spray" %% "spray-json" % "1.3.3",
    "org.parboiled" %% "parboiled-scala" % "1.1.8" % "compile",
    "org.specs2" %% "specs2-core" % "3.8.6" % "test"
  )

initialCommands in console += """
    import spray.json._
    import DefaultJsonProtocol._
    import lenses._
    import JsonLenses._
"""

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")
