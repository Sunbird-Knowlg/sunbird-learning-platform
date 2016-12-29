// Comment to get more information during initialization
logLevel := Level.Warn
scalaVersion := "2.10.6"
// The Typesafe repository

resolvers ++= Seq(
"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe repository mvn" at "http://repo.typesafe.com/typesafe/maven-releases/"
)
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")


