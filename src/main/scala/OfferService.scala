
import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object OfferService extends App with OfferJsonSupport {
    implicit val system: ActorSystem = ActorSystem("example-interview")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    implicit val clock: Clock = Clock.systemUTC()

    val filePath = "/experiments.json"

    Experiments.getSortedExperimentsFromFile(filePath) match {
        case Left(errorList) =>
            println(s"Errors found in file at ${filePath}")
            errorList.foreach(e => println(e.msg))
            println("Exiting OfferService")
            System.exit(1)
        case Right(experiments) => {

            val offerLogic = new OfferLogic(experiments)

            val phoneMatcher = "\\d{13}".r.pattern.asMatchPredicate()

            val validateScore: Double => Boolean = score => score >= 0.0D && score <= 1.0D

            val routes =
                (get & path("offers") & parameters("phone", "score".as[Double])) { (phone, score) =>
                    validate(phoneMatcher.test(phone), "Invalid Phone number") {
                        validate(validateScore(score), "Score must be between 0.0D and 1.0D") {
                            complete(offerLogic.getOffers(score).map(_.toOfferExternal))
                        }
                    }
                }

            val binding = Http().bindAndHandle(
                routes,
                "127.0.0.1",
                9000
            )
            Await.result(system.whenTerminated, Duration.Inf)
        }
    }

}

