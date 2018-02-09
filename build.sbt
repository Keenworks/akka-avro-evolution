name := "akka-avro-evolution"

organization := "com.keenworks"

version := "0.0.1"

scalaVersion := "2.12.4"

val akkaVersion = "2.4.20"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.30",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.4.18.2" % "test"
)

fork := true
