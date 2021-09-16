// val scala3Version = "3.0.2"

lazy val app = project
  .in(file("."))
  .settings(
    scalacOptions ++= Seq(
      "-language:postfixOps",
      "-language:implicitConversions",
    ),
    // scalaVersion := scala3Version,
    resolvers += "d4j-snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.discord4j" % "discord4j-core" % "3.2.0-SNAPSHOT",
      "org.immutables" % "value" % "2.8.8",
      ),
    name := "Test bot",
    Compile / packageBin / mainClass := Some("Bot"),
    Compile / run / mainClass := Some("Bot"),
  )
