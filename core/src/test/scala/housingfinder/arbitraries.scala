package housingfinder

import dev.profunktor.auth.jwt.JwtToken
import housingfinder.domain.auth.{CreateUserParam, LoginUserParam}
import housingfinder.domain.healthcheck.AppStatus
import housingfinder.domain.kijiji.{CreateListingParam, Listing, ListingId}
import housingfinder.generators._
import org.scalacheck.Arbitrary

object arbitraries {
  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)

  implicit val arbAppStatus: Arbitrary[AppStatus] =
    Arbitrary(genAppStatus)

  implicit val arbListingId: Arbitrary[ListingId] =
    Arbitrary(cbUuid[ListingId])

  implicit val arbJwtToken: Arbitrary[JwtToken] =
    Arbitrary(genJwtToken)

  implicit val arbCreateListingParam: Arbitrary[CreateListingParam] =
    Arbitrary(genCreateListingParam)

  implicit val arbCreateUserParam: Arbitrary[CreateUserParam] =
    Arbitrary(genCreateUserParam)

  implicit val arbLoginUserParam: Arbitrary[LoginUserParam] =
    Arbitrary(genLoginUserParam)
}