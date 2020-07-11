package utilities

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import cats.implicits._
import ciris.Secret
import dev.profunktor.auth.jwt.{JwtSecretKey, JwtToken}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.string.{Url, ValidBigDecimal}
import eu.timepit.refined.types.string.NonEmptyString
import housingfinder.config.data.PasswordSalt
import housingfinder.domain.auth._
import housingfinder.domain.healthcheck.{AppStatus, PostgresStatus, RedisStatus}
import housingfinder.domain.listings._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.scalacheck.{Arbitrary, Gen}
import squants.market.{CAD, Money}

object generators {

  def cbUuid[A: Coercible[UUID, *]]: Gen[A] =
    Gen.uuid.map(_.coerce[A])

  def cbStr[A: Coercible[String, *]]: Gen[A] =
    genNonEmptyString.map(_.coerce[A])

  def cbBool[A: Coercible[Boolean, *]]: Gen[A] =
    Arbitrary.arbBool.arbitrary.map(_.coerce[A])

  val genNonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  val genMoney: Gen[Money] =
    Gen.posNum[Double].map(n => CAD(BigDecimal(n)))

  val genLocalDateTime: Gen[LocalDateTime] = {
    val utc = ZoneOffset.UTC

    val rangeStart = LocalDateTime
      .now(utc)
      .minusYears(5)
      .toEpochSecond(utc)
    val currentYear = LocalDateTime.now(utc).getYear
    val rangeEnd =
      LocalDateTime.of(currentYear, 1, 1, 0, 0, 0).toEpochSecond(utc)

    Gen
      .choose(rangeStart, rangeEnd)
      .map(i => LocalDateTime.ofEpochSecond(i, 0, utc))
  }

  val genJwtToken: Gen[JwtToken] =
    genNonEmptyString.map(JwtToken)

  val genListing: Gen[Listing] =
    for {
      i <- cbUuid[ListingId]
      t <- cbStr[Title]
      a <- cbStr[Address]
      p <- genMoney
      de <- cbStr[Description]
      da <- genLocalDateTime
      u <- cbStr[ListingUrl]
    } yield Listing(i, t, a, p, de, da, u)

  val genCreateListing: Gen[CreateListing] =
    for {
      t <- cbStr[Title]
      a <- cbStr[Address]
      p <- genMoney
      de <- cbStr[Description]
      da <- genLocalDateTime
      u <- cbStr[ListingUrl]
    } yield CreateListing(t, a, p, de, da, u)

  val genAppStatus: Gen[AppStatus] =
    for {
      r <- cbBool[RedisStatus]
      p <- cbBool[PostgresStatus]
    } yield AppStatus(r, p)

  val genCreateListingParam: Gen[CreateListingParam] =
    for {
      t <- genStrRefinedUnsafe(TitleParam.apply)
      a <- genStrRefinedUnsafe(AddressParam.apply)
      p <-
        Gen
          .posNum[Double]
          .map(l => Refined.unsafeApply[String, ValidBigDecimal](l.toString))
          .map(PriceParam.apply)
      de <- genStrRefinedUnsafe(DescriptionParam.apply)
      da <- genLocalDateTime
      u <-
        genNonEmptyString
          .map(s => "http://" + s + ".com")
          .map(ListingUrlParam.apply _ compose Refined.unsafeApply[String, Url])
    } yield CreateListingParam(t, a, p, de, da, u)

  val genCreateUserParam: Gen[CreateUserParam] =
    for {
      u <- genStrRefinedUnsafe(UsernameParam.apply)
      p <- genStrRefinedUnsafe(PasswordParam.apply)
    } yield CreateUserParam(u, p)

  val genLoginUserParam: Gen[LoginUserParam] =
    for {
      u <- genStrRefinedUnsafe(UsernameParam.apply)
      p <- genStrRefinedUnsafe(PasswordParam.apply)
    } yield LoginUserParam(u, p)

  def genStrRefinedUnsafe[P, A](c: Refined[String, P] => A): Gen[A] =
    genNonEmptyString.map(s => c(Refined.unsafeApply(s)))

  val genJwtSecretKey: Gen[JwtSecretKey] =
    genNonEmptyString.map(JwtSecretKey)

  val genPasswordSalt: Gen[PasswordSalt] =
    Secret("secret": NonEmptyString).coerce[PasswordSalt]
}
