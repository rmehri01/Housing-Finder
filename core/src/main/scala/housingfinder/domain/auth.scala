package housingfinder.domain

import java.util.UUID

import io.estatico.newtype.macros.newtype

object auth {
  @newtype case class UserId(value: UUID)
  // TODO: restrictions on usernames and passwords
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)


}
