lazy val producer = project.in(file("producer"))
lazy val consumer = project.in(file("consumer"))

lazy val root = (project in file("."))
  .aggregate(producer, consumer)
  .settings(
    name := "Log Analysis",
    version := "0.1",
    scalaVersion := "2.13.8"
  )
