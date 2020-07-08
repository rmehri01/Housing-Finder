package housingfinder.domain

import java.util.UUID

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder
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
  @newtype case class UsernameParam(value: NonEmptyString) {
    def toDomain: Username = Username(value.value)
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class CreateUserParam(
      username: UsernameParam,
      password: PasswordParam
  )

  case class UsernameInUse(username: Username) extends NoStackTrace
  case class InvalidUserOrPassword(username: Username) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  // user login
  case class LoginUserParam(
      username: UsernameParam,
      password: PasswordParam
  )

  // admin auth
  @newtype case class ClaimContent(uuid: UUID)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] =
      Decoder.forProduct1("uuid")(ClaimContent.apply)
  }

}
