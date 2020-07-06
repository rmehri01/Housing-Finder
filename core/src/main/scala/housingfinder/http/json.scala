package housingfinder.http

import cats.Applicative
import dev.profunktor.auth.jwt.JwtToken
import housingfinder.domain.auth.{CreateUser, LoginUser}
import housingfinder.domain.kijiji.{CreateListingParam, Listing}
import housingfinder.http.auth.users.User
import io.circe.generic.semiauto._
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import squants.market.Money

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]
      : EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

private[http] trait JsonCodecs {
  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.asInstanceOf[B])

  implicit val listingEncoder: Encoder[Listing] =
    deriveEncoder[Listing]

  implicit val createListingDecoder: Decoder[CreateListingParam] =
    deriveDecoder[CreateListingParam]

  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("accessToken")(_.value)

  implicit val userDecoder: Decoder[User] =
    deriveDecoder[User]

  implicit val createUserDecoder: Decoder[CreateUser] =
    deriveDecoder[CreateUser]

  implicit val loginUserDecoder: Decoder[LoginUser] =
    deriveDecoder[LoginUser]
}
