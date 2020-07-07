package housingfinder

import java.time.LocalDateTime
import java.util.UUID

import dev.profunktor.auth.jwt.JwtToken
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidBigDecimal
import housingfinder.domain.auth._
import housingfinder.domain.healthcheck.{AppStatus, PostgresStatus, RedisStatus}
import housingfinder.domain.kijiji._
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

  val genLocalDateTime: Gen[LocalDateTime] =
    Gen.calendar.map(cal =>
      LocalDateTime.ofInstant(cal.toInstant, cal.getTimeZone.toZoneId)
    )

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
    } yield Listing(i, t, a, p, de, da)

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
    } yield CreateListingParam(t, a, p, de, da)

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

}
