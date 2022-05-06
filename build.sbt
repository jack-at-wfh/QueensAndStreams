ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "QueensAndStreams",
    libraryDependencies ++= List(
      Libraries.zio,
      Libraries.zioStreams
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
