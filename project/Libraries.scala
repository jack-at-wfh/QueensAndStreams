import sbt._

object Libraries {
  import Versions._
  val zio = "dev.zio" %% "zio" % zioVersion
  val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
  val zioTest = "dev.zio" %% "zio-test" % zioVersion
}

object Versions {
  val zioVersion = "2.0.1"
}
