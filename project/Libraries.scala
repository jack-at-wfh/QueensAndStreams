import sbt._

object Libraries {
  import Versions._
  val zio = "dev.zio" %% "zio" % zioVersion
  val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
  val zioTest = "dev.zio" %% "zio-test" % zioVersion
  val scalaNLP = "org.scalanlp" %% "breeze" % breezeVersion
  val scalaNLPViz = "org.scalanlp" %% "breeze-viz" % breezeVersion
}

object Versions {
  val zioVersion = "2.0.1"
  val breezeVersion = "2.0.1-RC1"
}
