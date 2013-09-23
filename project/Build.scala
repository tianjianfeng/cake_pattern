import sbt._
import Keys._
import play.Project._
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object ApplicationBuild extends Build {

    val appName = "cake_pattern"
    val appVersion = "1.0-SNAPSHOT"
        
    lazy val jacoco_settings = Defaults.defaultSettings ++ Seq(jacoco.settings: _*)

    val main = play.Project(appName, appVersion, Seq(), settings = jacoco_settings).settings(
        //        resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",

        parallelExecution in jacoco.Config := false,
        jacoco.reportFormats in jacoco.Config := Seq(XMLReport("utf-8"), HTMLReport("utf-8")),
        jacoco.includes in jacoco.Config := Seq("controllers.*", "models.*", "services.*"),
        jacoco.excludes in jacoco.Config := Seq("views*", "*Routes*", "controllers*routes*", "controllers*Reverse*", "controllers*javascript*", "controller*ref*", "*$$*", "*package*", "*Controller*"))

    }
