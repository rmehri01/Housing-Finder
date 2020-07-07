package housingfinder

import dev.profunktor.auth.jwt.JwtToken
import housingfinder.domain.auth.{Password, Username}
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

  implicit val arbUsername: Arbitrary[Username] =
    Arbitrary(cbStr[Username])

  implicit val arbPassword: Arbitrary[Password] =
    Arbitrary(cbStr[Password])

  implicit val arbJwtToken: Arbitrary[JwtToken] =
    Arbitrary(genJwtToken)
}
