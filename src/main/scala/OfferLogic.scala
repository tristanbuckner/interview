import java.time.{Clock, LocalDate}

import scala.annotation.tailrec

case class OfferLogic(experiments: IndexedSeq[Experiment]) {

    //Pre-sort the scores so we can use take instead of filter
    private val optimizedExperiments = experiments.map(e => e.copy(offers=e.offers.sortBy(_.minScore)))

    /**
     * Search the sorted experiments for the current active experiment (Log(N)) and then filter by score threshold on
     * the presorted offers (Log(K) where K is the number of matching offers)
     *
     * @param score
     * @param clock
     * @return
     */
    def getOffers(score: Double)(implicit clock: Clock): List[Offer] = {
        val now = LocalDate.now(clock)
        findActive(now).toList.flatMap(_.offers.takeWhile(_.minScore <= score))
    }

    /**
     * Inspired by the binary search implementation found in scala.collection.Searching
     * @param currentDay
     * @return
     */
    private def findActive(currentDay: LocalDate): Option[Experiment] = {
        @tailrec
        def binarySearchExperiments(currentDay: LocalDate, from: Int, to: Int): Option[Experiment] = {
            if (to == from) None else {
                val idx = from + (to - from - 1) / 2
                val exp = optimizedExperiments(idx)
                (!currentDay.isBefore(exp.startDate), !currentDay.isAfter(exp.endDate)) match {
                    case (false, _) => binarySearchExperiments(currentDay, from, idx)
                    case (_, false) => binarySearchExperiments(currentDay, idx + 1, to)
                    case (true, true) => Some(exp)
                }
            }
        }

        binarySearchExperiments(currentDay, 0, experiments.length)
    }


}

