package housingfinder

import housingfinder.domain.healthcheck.AppStatus
import housingfinder.domain.kijiji.{Listing, ListingId}
import housingfinder.generators._
import org.scalacheck.Arbitrary

object arbitraries {
  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)

  implicit val arbAppStatus: Arbitrary[AppStatus] =
    Arbitrary(genAppStatus)

  implicit val arbListingId: Arbitrary[ListingId] =
    Arbitrary(cbUuid[ListingId])
}
