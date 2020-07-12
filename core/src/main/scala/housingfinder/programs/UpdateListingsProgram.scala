package housingfinder.programs

import cats.Monad
import cats.implicits._
import housingfinder.algebras.{Listings, Scraper}

final class UpdateListingsProgram[F[_]: Monad](
    listings: Listings[F],
    scraper: Scraper[F]
) {

  def scrapeAndUpdate: F[Unit] =
    scraper.run.flatMap(listings.addAll)

}
