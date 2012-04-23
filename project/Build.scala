import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {
  val appName         = "katze"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  val core = Project("core", file("modules/core"))

  val cli  = Project("cli", file("modules/cli")).dependsOn(
    core
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).dependsOn(
    core
  )
}
