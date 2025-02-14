package housingfinder.domain

import io.estatico.newtype.macros.newtype

import scala.util.control.NoStackTrace

object scraper {
  @newtype case class Html(value: String)
  @newtype case class KijijiUrl(value: String)
  @newtype case class RelListingUrl(value: String)

  @newtype case class ErrorMessage(value: String)
  case class KijijiConnectionError(cause: ErrorMessage) extends NoStackTrace
}
