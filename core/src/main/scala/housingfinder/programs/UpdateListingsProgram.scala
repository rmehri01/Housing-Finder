package housingfinder.programs

import cats.Monad
import cats.implicits._
import housingfinder.algebras.{Listings, Scraper}
import housingfinder.http.clients.KijijiClient

final class UpdateListingsProgram[F[_]: Monad](
    listings: Listings[F],
    scraper: Scraper[F],
    kijji: KijijiClient[F]
) {

  def scrapeAndUpdate: F[Unit] =
    scraper.run.flatMap(listings.addAll)

}
