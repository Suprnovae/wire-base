import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "wire-engine"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.postgresql" % "postgresql" % "9.2-1003-jdbc4"
    //"org.spec2" %% "specs2" % "2.1.1" % "test"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
