package housingfinder.programs

import cats.Monad
import cats.implicits._
import housingfinder.algebras.{Listings, Scraper}

/** Program for getting listings off of a website and then adding them to the database.
  *
  * Uses the client to retrieve the HTML, the scraper to extract listings, and the listings database
  * to add the results for later use.
  *
  * The current implementation both gets and scrapes the pages in parallel.
  */
final class UpdateListingsProgram[F[_]: Monad](
    listings: Listings[F],
    scraper: Scraper[F]
) {

  /** Uses the scraper to get listings and then adds them to the database. */
  def scrapeAndUpdate: F[Unit] =
    scraper.run.flatMap(ls => listings.addAll(ls))

}
