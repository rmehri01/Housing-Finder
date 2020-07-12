package housingfinder.algebras

import housingfinder.domain.listings.CreateListing

trait Scraper[F[_]] {
  def run: F[List[CreateListing]]
}
