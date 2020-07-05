package housingfinder.domain

import java.util.UUID

import io.estatico.newtype.macros.newtype
import squants.market.Money

object kijiji {
  @newtype case class ListingId(value: UUID)
  @newtype case class Title(value: String)
  @newtype case class Address(value: String)
  @newtype case class Description(value: String)

  // TODO: assert non-empty string when creating

  case class Listing(
      uuid: ListingId,
      title: Title,
      address: Address,
      price: Money,
      description: Description
  )
}
