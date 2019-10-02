name := "yamlLoaders"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.yaml" % "snakeyaml" % "1.23", // see also circe-yaml
  "com.typesafe.akka" %% "akka-actor" % "2.5.25",
  "com.typesafe.akka" %% "akka-stream" % "2.5.25",
  "com.typesafe.akka" %% "akka-http" % "10.1.8"
)


