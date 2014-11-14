scalaVersion := "2.11.4"

resolvers += "spray" at "http://repo.spray.io"

libraryDependencies ++=
  Seq(
    "io.spray" %% "spray-json" % "1.3.1",
    "org.parboiled" %% "parboiled-scala" % "1.1.6" % "compile",
    "org.specs2" %% "specs2-core" % "2.3.11" % "test")

initialCommands in console += """
    import spray.json._
    import DefaultJsonProtocol._
    import lenses._
    import JsonLenses._
"""

crossScalaVersions := Seq("2.10.4", "2.11.4")