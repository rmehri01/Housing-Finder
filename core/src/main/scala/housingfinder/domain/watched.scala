package housingfinder.domain

import housingfinder.domain.listings.ListingId

import scala.util.control.NoStackTrace

object watched {
  case class AlreadyWatched(listingId: ListingId) extends NoStackTrace
}
