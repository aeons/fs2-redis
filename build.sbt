lazy val root = (project in file(".")).settings(
  inThisBuild(
    Seq(
      organization := "dk.aeons",
      scalaVersion := "2.12.3",
      version := "0.0.1-SNAPSHOT"
    )),
  name := "fs2-redis",
  libraryDependencies ++= Seq(
    "co.fs2"       %% "fs2-io"            % "0.10.0-M6",
    "org.tpolecat" %% "atto-core"         % "0.6.1-M5",
    "org.specs2"   %% "specs2-core"       % "4.0.0-RC4" % "test",
    "org.specs2"   %% "specs2-scalacheck" % "4.0.0-RC4" % "test"
  )
)
