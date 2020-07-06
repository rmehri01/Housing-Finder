package housingfinder.config

import ciris._
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import scala.concurrent.duration.FiniteDuration

object data {
  @newtype case class TokenExpiration(value: FiniteDuration)

  @newtype case class PasswordSalt(value: Secret[NonEmptyString])
}
