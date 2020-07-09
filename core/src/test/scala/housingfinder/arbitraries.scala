package housingfinder

import dev.profunktor.auth.jwt.JwtToken
import housingfinder.domain.auth._
import housingfinder.domain.healthcheck.AppStatus
import housingfinder.domain.listings._
import housingfinder.generators._
import org.scalacheck.Arbitrary

object arbitraries {
  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)

  implicit val arbCreateListing: Arbitrary[CreateListing] =
    Arbitrary(genCreateListing)

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

  implicit val arbUsername: Arbitrary[Username] =
    Arbitrary(cbStr[Username])

  implicit val arbPassword: Arbitrary[Password] =
    Arbitrary(cbStr[Password])
}
