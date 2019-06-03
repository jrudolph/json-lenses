scalaVersion := "2.12.8"

libraryDependencies ++=
  Seq(
    "io.spray" %% "spray-json" % "1.3.5",
    if (scalaVersion.value.startsWith("2.13"))
      "org.parboiled" % "parboiled-scala_2.13.0-M5" % "1.3.0" % Compile
    else
      "org.parboiled" %% "parboiled-scala" % "1.3.0" % Compile,
    if (scalaVersion.value.startsWith("2.10"))
      "org.specs2" %% "specs2-core" % "3.10.0" % Test
    else
      "org.specs2" %% "specs2-core" % "4.5.1" % Test
  )

initialCommands in console += """
    import spray.json._
    import DefaultJsonProtocol._
    import lenses._
    import JsonLenses._
"""

crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.8", "2.13.0")

scalacOptions ++= Seq("-deprecation", "-feature")