package housingfinder.domain

import java.util.UUID

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import squants.market.Money

object kijiji {
  @newtype case class ListingId(value: UUID)
  @newtype case class Address(value: NonEmptyString)
  @newtype case class Feature(value: NonEmptyString)
  @newtype case class Description(value: NonEmptyString)

  case class Listing(
      uuid: ListingId,
      address: Address,
      price: Money,
      features: List[Feature],
      description: Description
  )
}
