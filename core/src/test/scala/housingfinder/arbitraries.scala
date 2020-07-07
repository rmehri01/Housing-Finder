package housingfinder

import housingfinder.domain.healthcheck.AppStatus
import housingfinder.domain.kijiji.Listing
import housingfinder.generators._
import org.scalacheck.Arbitrary

object arbitraries {
  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)

  implicit val arbAppStatus: Arbitrary[AppStatus] =
    Arbitrary(genAppStatus)
}
