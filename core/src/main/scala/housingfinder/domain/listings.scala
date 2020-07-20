package housingfinder.domain

import java.time.LocalDateTime
import java.util.UUID

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import squants.market.Money

object listings {
  @newtype case class ListingId(value: UUID)
  @newtype case class Title(value: String)
  @newtype case class Address(value: String)
  @newtype case class Description(value: String)
  @newtype case class ListingUrl(value: String)

  case class Listing(
      uuid: ListingId,
      title: Title,
      address: Address,
      price: Option[Money],
      description: Description,
      datePosted: LocalDateTime,
      url: ListingUrl
  )

  // create listing
  @newtype case class TitleParam(value: NonEmptyString)
  @newtype case class AddressParam(value: NonEmptyString)
  @newtype case class DescriptionParam(value: NonEmptyString)
  @newtype case class ListingUrlParam(value: String Refined Url)

  case class CreateListingParam(
      title: TitleParam,
      address: AddressParam,
      price: Option[Money],
      description: DescriptionParam,
      datePosted: LocalDateTime,
      url: ListingUrlParam
  ) {
    def toDomain: CreateListing =
      CreateListing(
        Title(title.value.value),
        Address(address.value.value),
        price,
        Description(description.value.value),
        datePosted,
        ListingUrl(url.value.value)
      )
  }

  case class CreateListing(
      title: Title,
      address: Address,
      price: Option[Money],
      description: Description,
      dateTime: LocalDateTime,
      listingUrl: ListingUrl
  )
}
