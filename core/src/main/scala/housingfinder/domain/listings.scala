package housingfinder.domain

import java.time.LocalDateTime
import java.util.UUID

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.ValidBigDecimal
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import squants.market.{CAD, Money}

object listings {
  @newtype case class ListingId(value: UUID)
  @newtype case class Title(value: String)
  @newtype case class Address(value: String)
  @newtype case class Description(value: String)

  case class Listing(
      uuid: ListingId,
      title: Title,
      address: Address,
      price: Money,
      description: Description,
      datePosted: LocalDateTime
  )

  // create listing
  @newtype case class TitleParam(value: NonEmptyString)
  @newtype case class AddressParam(value: NonEmptyString)
  @newtype case class PriceParam(value: String Refined ValidBigDecimal)
  @newtype case class DescriptionParam(value: NonEmptyString)

  case class CreateListingParam(
      titleParam: TitleParam,
      addressParam: AddressParam,
      priceParam: PriceParam,
      descriptionParam: DescriptionParam,
      dateParam: LocalDateTime
  ) {
    def toDomain: CreateListing =
      CreateListing(
        Title(titleParam.value.value),
        Address(addressParam.value.value),
        CAD(BigDecimal(priceParam.value.value)),
        Description(descriptionParam.value.value),
        dateParam
      )
  }

  case class CreateListing(
      title: Title,
      address: Address,
      price: Money,
      description: Description,
      dateTime: LocalDateTime
  )
}
