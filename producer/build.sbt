name := "Producer"

version := "1.0"

scalaVersion := "2.13.8"

val sparkVersion = "3.2.0"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.1.0",
  )
