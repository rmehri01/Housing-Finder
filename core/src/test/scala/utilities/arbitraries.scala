package utilities

import cats.data.NonEmptyList
import dev.profunktor.auth.jwt.{JwtSecretKey, JwtToken}
import housingfinder.config.data.PasswordSalt
import housingfinder.domain.auth._
import housingfinder.domain.healthcheck.AppStatus
import housingfinder.domain.listings._
import org.scalacheck.{Arbitrary, Gen}
import utilities.generators._

object arbitraries {
  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)

  implicit val arbCreateListing: Arbitrary[CreateListing] =
    Arbitrary(genCreateListing)

  implicit val arbAppStatus: Arbitrary[AppStatus] =
    Arbitrary(genAppStatus)

  implicit val arbListingId: Arbitrary[ListingId] =
    Arbitrary(cbUuid[ListingId])

  implicit val arbTitle: Arbitrary[Title] =
    Arbitrary(cbStr[Title])

  implicit val arbNonEmptyListings: Arbitrary[NonEmptyList[CreateListing]] =
    Arbitrary(
      Gen.nonEmptyListOf(genCreateListing).map(NonEmptyList.fromListUnsafe)
    )

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

  implicit val arbJwtSecretKey: Arbitrary[JwtSecretKey] =
    Arbitrary(genJwtSecretKey)

  implicit val arbPasswordSalt: Arbitrary[PasswordSalt] =
    Arbitrary(genPasswordSalt)
}
