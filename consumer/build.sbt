name := "Consumer"

version := "1.0"

scalaVersion := "2.13.8"

val sparkVersion = "3.2.0"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.1.0",
  "mysql" % "mysql-connector-java" % "8.0.27",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion
)
