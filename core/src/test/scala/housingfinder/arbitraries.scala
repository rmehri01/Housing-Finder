package housingfinder

import java.util.UUID

import housingfinder.domain.kijiji.Listing
import housingfinder.generators._
import io.estatico.newtype.Coercible
import org.scalacheck.Arbitrary
import squants.market.Money

object arbitraries {

  implicit def arbCoercibleUUID[A: Coercible[UUID, *]]: Arbitrary[A] =
    Arbitrary(cbUuid[A])

  implicit def arbCoercibleStr[A: Coercible[String, *]]: Arbitrary[A] =
    Arbitrary(cbStr[A])

  implicit val arbMoney: Arbitrary[Money] =
    Arbitrary(genMoney)

  implicit val arbListing: Arbitrary[Listing] =
    Arbitrary(genListing)
}
