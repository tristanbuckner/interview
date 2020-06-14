import org.scalatest.funsuite.AnyFunSuite

class ExperimentsTest extends AnyFunSuite {

    test("file loading should enforce validations") {

        val errorMessages: List[String] = Experiments.getSortedExperimentsFromFile("/break_everything.json")
          .left.getOrElse(Nil).map(_.msg)

        assert(errorMessages.exists(_.startsWith("Overlapping experiments detected")))
        assert(errorMessages.exists(_.startsWith("Experiment name must not be greater than 20 characters")))
        assert(errorMessages.exists(_.startsWith("Min Score must be in [0.0, 1.0]")))
        assert(errorMessages.exists(_.startsWith("Amount must be in [10, 50000]")))
        assert(errorMessages.exists(_.startsWith("Fee must be in [0, 100]")))
        assert(errorMessages.exists(_.startsWith("Term must be in [1, 365]")))
        assert(errorMessages.exists(_.startsWith("endDate cannot precede startDate")))

    }

}
