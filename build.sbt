name := "aria-loan-service-challenge"

scalaVersion := "2.11.12"

mainClass := Some("OfferService")

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.25",
  "com.typesafe.akka" %% "akka-stream" % "2.5.25",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",

  "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.26" % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.12" % Test,
  "org.scalactic" %% "scalactic" % "3.1.2" % Test,
  "org.scalatest" %% "scalatest" % "3.1.2" % Test

)


