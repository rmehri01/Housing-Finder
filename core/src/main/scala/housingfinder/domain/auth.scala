package housingfinder.domain

import java.util.UUID

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import javax.crypto.Cipher

import scala.util.control.NoStackTrace

object auth {
  @newtype case class UserId(value: UUID)
  @newtype case class Username(value: String)
  @newtype case class Password(value: String)

  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  // TODO: restrictions on username and password params
  // user registration
  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: Username = Username(value.value.toLowerCase)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class CreateUser(
      username: UserNameParam,
      password: PasswordParam
  )

  case class UserNameInUse(username: Username) extends NoStackTrace
  case class InvalidUserOrPassword(username: Username) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  // user login
  case class LoginUser(
      username: UserNameParam,
      password: PasswordParam
  )
}
