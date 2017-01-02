import sbt._
import Keys._
import play.sbt._

object ApplicationBuild extends Build {

    val root = Project("search-manager", file("search-manager"))
        .settings(
            version := Pom.version(baseDirectory.value),
            libraryDependencies ++= Pom.dependencies(baseDirectory.value))

    override def rootProject = Some(root)
}
