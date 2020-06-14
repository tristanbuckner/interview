import java.time.LocalDate

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, JsonParser, ParserInput, RootJsonFormat}

object Experiments extends OfferJsonSupport {

    implicit object LocalDateOrdering extends Ordering[LocalDate] {
        override def compare(x: LocalDate, y: LocalDate): Int = x.compareTo(y)
    }

    /**
     * Helper method for loading an experiments file and running validations
     *
     * @param path "/" is the root of the resources dir
     * @return
     */
    def getSortedExperimentsFromFile(path: String): Either[List[ValidationError], IndexedSeq[Experiment]] = {
        val experiments =
            JsonParser(ParserInput(getClass.getResourceAsStream(path)
              .readAllBytes()))
              .convertTo[ExperimentsFile]

        experiments.validated.right.map(_.experiments)
    }
}

case class ExperimentsFile(experiments: IndexedSeq[Experiment]) extends Validatable[ExperimentsFile] {
    override def validated: Either[List[ValidationError], ExperimentsFile] = {
        import Experiments.LocalDateOrdering

        //Sort experiments on start date and check them against their neighbors
        val experimentsSorted: IndexedSeq[Experiment] = experiments.sortBy(e => e.startDate)
        val overlapErrors = experimentsSorted.zip(experimentsSorted.tail)
          .flatMap(p => if (!p._2.startDate.isAfter(p._1.endDate))
              List(ValidationError(s"Overlapping experiments detected: [${p._1.startDate}, ${p._1.endDate}] and [${p._2.startDate}, ${p._2.endDate}]"))
          else Nil)

        val validated = experiments.map(_.validated)

        val errors = validated.flatMap(_.left.getOrElse(Nil))

        overlapErrors.toList ++ errors match {
            case Nil => Right(this.copy(experiments = experimentsSorted))
            case validationErrors => Left(validationErrors)
        }

    }
}

case class Experiment(name: String, startDate: LocalDate, endDate: LocalDate, offers: List[Offer]) extends Validatable[Experiment] {
    override def validated: Either[List[ValidationError], Experiment] = {

        List(if (name.length > 20) List(ValidationError(s"Experiment name must not be greater than 20 characters, was ${name.length} characters long")) else Nil,
            if (endDate.isBefore(startDate)) List(ValidationError(s"endDate cannot precede startDate: startDate = $startDate endDate = $endDate")) else Nil,
            offers.map(_.validated).flatMap(_.left.getOrElse(Nil))
        ).flatten match {
            case Nil => Right(this)
            case validationErrors => Left(validationErrors)
        }
    }
}

case class Offer(minScore: Double, amount: Int, fee: Int, term: Int) extends Validatable[Offer] {
    override def validated: Either[List[ValidationError], Offer] = {

        List(if (!(minScore >= 0.0D && minScore <= 1.0D)) List(ValidationError(s"Min Score must be in [0.0, 1.0], was $minScore")) else Nil,
            if (!(amount >= 10 && amount <= 50000)) List(ValidationError(s"Amount must be in [10, 50000], was $amount")) else Nil,
            if (!(fee >= 0 && fee <= 100)) List(ValidationError(s"Fee must be in [0, 100], was $fee")) else Nil,
            if (!(term >= 1 && term <= 365)) List(ValidationError(s"Term must be in [1, 365], was $term")) else Nil
        ).flatten match {
            case Nil => Right(this)
            case validationErrors => Left(validationErrors)
        }

    }

    def toOfferExternal: OfferExternal = OfferExternal(amount, fee, term)
}

case class OfferExternal(amount: Int, fee: Int, term: Int)

/**
 * Akka spray protocols for the above case classes
 */
trait OfferJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val localDateFormat: JsonFormat[LocalDate] = new JsonFormat[LocalDate]() {
        override def read(json: JsValue): LocalDate = json match {
            case JsString(value) => LocalDate.parse(value)
            case _ => throw new IllegalArgumentException("Bad json format")

        }

        override def write(obj: LocalDate): JsValue = JsString(obj.toString)
    }


    implicit val offerFormat: RootJsonFormat[Offer] = jsonFormat4(Offer)
    implicit val offerExternalFormat: RootJsonFormat[OfferExternal] = jsonFormat3(OfferExternal)

    implicit val experimentFormat: RootJsonFormat[Experiment] = jsonFormat4(Experiment)
    implicit val experimentsFileFormat: RootJsonFormat[ExperimentsFile] = jsonFormat1(ExperimentsFile)
}

/**
 * A trait for validating implementing classes, collecting all encountered errors. In the real world
 * I'd use an existing framework like Validation in cats
 *
 * @tparam T
 */
trait Validatable[T <: Validatable[T]] {
    def validated: Either[List[ValidationError], T]
}

case class ValidationError(msg: String)
