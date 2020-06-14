
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

    try {

        val validatedSortedExperiments = Experiments.getSortedExperimentsFromFile(filePath) match {
            case Left(errorList) =>
                throw ValidationException(errorList)
            case Right(experiments) => {
                experiments
            }
        }

        val offerLogic = OfferLogic(validatedSortedExperiments)

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

        val port = 9000
        val binding = Http().bindAndHandle(
            routes,
            "127.0.0.1",
            port
        )
        println(s"Server started on port $port")
        Await.result(system.whenTerminated, Duration.Inf)
    } catch {
        case ValidationException(errors) => println(s"Errors found in file at ${filePath}:")
            errors.foreach(e => println("\t" + e.msg))
            println("Exiting OfferService")
            System.exit(1)
        case e: Exception => e.printStackTrace()
            System.exit(1)
    }
}

