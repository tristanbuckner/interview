
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object ExampleApp extends App {
  implicit val system: ActorSystem = ActorSystem("example-interview")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val routes =
    (get & path("hello")) {
      complete("Hello world!")
    } ~ (get & path("goodbye")) {
      system.terminate
      complete("Goodbye!")
    }

  val binding = Http().bindAndHandle(
    routes,
    "127.0.0.1",
    8080
  )
  Await.result(system.whenTerminated, Duration.Inf)
}
