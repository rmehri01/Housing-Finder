package housingfinder

import java.time.LocalDateTime
import java.util.UUID

import dev.profunktor.auth.jwt.JwtToken
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
    Gen.posNum[Long].map(n => CAD(BigDecimal(n)))

  // TODO: not sure about this
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

  val genCreateListing: Gen[CreateListing] =
    for {
      t <- cbStr[Title]
      a <- cbStr[Address]
      p <- genMoney
      de <- cbStr[Description]
      da <- genLocalDateTime
    } yield CreateListing(t, a, p, de, da)

  val genAppStatus: Gen[AppStatus] =
    for {
      r <- cbBool[RedisStatus]
      p <- cbBool[PostgresStatus]
    } yield AppStatus(r, p)

}
