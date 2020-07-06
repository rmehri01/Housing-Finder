package housingfinder.config

import ciris._
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object data {
  @newtype case class PasswordSalt(value: Secret[NonEmptyString])
}
