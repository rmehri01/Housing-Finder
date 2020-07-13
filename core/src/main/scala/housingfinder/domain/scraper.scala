package housingfinder.domain

import scala.util.control.NoStackTrace

object scraper {
  case class KijijiConnectionError(cause: String) extends NoStackTrace
}
