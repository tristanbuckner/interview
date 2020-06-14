import java.time.{Clock, LocalDate, ZoneId}

import org.scalatest.funsuite.AnyFunSuite

class OfferLogicTest extends AnyFunSuite {

    test("the correct experiment should be returned based on the current day") {

        val offerLogic = getLogicForFile("/experiment_gap.json")

        assert(offerLogic.getOffers(1D)(clockAt("2020-05-31")).length == 2)
        assert(offerLogic.getOffers(1D)(clockAt("2020-06-01")).isEmpty)
        assert(offerLogic.getOffers(1D)(clockAt("2020-08-29")).length == 3)

    }

    test("endpoints should work the same as days in the middle") {

        val offerLogic = getLogicForFile("/experiment_gap.json")

        assert(offerLogic.getOffers(1D)(clockAt("2020-05-01")).length == 2)
        assert(offerLogic.getOffers(1D)(clockAt("2020-05-15")).length == 2)
        assert(offerLogic.getOffers(1D)(clockAt("2020-05-31")).length == 2)
    }

    test("ensure score filtering works") {
        val offerLogic = getLogicForFile("/score_filter_test.json")

        implicit val clock: Clock = clockAt("2020-05-15")

        (0 to 10).map(i => i * 0.1D).foreach(score => assert(offerLogic.getOffers(score).forall(_.minScore <= score)))

    }


    private def clockAt(dateString: String) = Clock.fixed(LocalDate.parse(dateString).atStartOfDay(ZoneId.of("UTC")).toInstant, ZoneId.of("UTC"))

    private def getLogicForFile(path: String) = {
        val experiments = Experiments.getSortedExperimentsFromFile(path) match {
            case Left(errorList) => errorList.foreach(println)
                throw new RuntimeException("Invalid experiments file")
            case Right(experiments) => experiments
        }

        new OfferLogic(experiments)
    }

}
