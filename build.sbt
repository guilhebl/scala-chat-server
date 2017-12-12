name := """scala-chat-room"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += filters

libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"

lazy val akkaVersion = "2.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0-M3" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
