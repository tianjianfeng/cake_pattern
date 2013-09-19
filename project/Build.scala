import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName = "cake_pattern"
    val appVersion = "1.0-SNAPSHOT"

    val appDependencies = Seq(
        "org.mockito" % "mockito-all" % "1.9.5",
        "org.reactivemongo" % "play2-reactivemongo_2.10" % "0.9")

    val main = play.Project(appName, appVersion, appDependencies).settings(
        resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/")

}
