import sbt._
import Keys._
import play.Project._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys


name := "cake_pattern"

version := "0.0.1"

lazy val jacoco_settings = Defaults.defaultSettings ++ Seq(jacoco.settings: _*)

resolvers ++= Seq ("Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
	 "Mandubian repository snapshots" at "https://github.com/mandubian/mandubian-mvn/raw/master/snapshots/")

libraryDependencies ++= Seq(
          "org.mockito" % "mockito-all" % "1.9.5",
        "org.jacoco" % "org.jacoco.core" % "0.6.3.201306030806",
        "org.jacoco" % "org.jacoco.report" % "0.6.3.201306030806",
        "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0-SNAPSHOT"
)

playScalaSettings