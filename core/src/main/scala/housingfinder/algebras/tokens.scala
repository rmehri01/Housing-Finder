package housingfinder.algebras

import cats.effect.Sync
import cats.implicits._
import dev.profunktor.auth.jwt._
import housingfinder.config.data.{JwtSecretKeyConfig, TokenExpiration}
import housingfinder.effects.GenUUID
import io.circe.syntax._
import pdi.jwt.{JwtAlgorithm, JwtClaim}

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object LiveTokens {
  def make[F[_]: Sync](
      tokenConfig: JwtSecretKeyConfig,
      tokenExpiration: TokenExpiration
  ): F[Tokens[F]] =
    Sync[F].delay(java.time.Clock.systemUTC).map { implicit jClock =>
      new LiveTokens[F](tokenConfig, tokenExpiration)
    }
}

final class LiveTokens[F[_]: GenUUID: Sync] private (
    config: JwtSecretKeyConfig,
    exp: TokenExpiration
)(implicit ev: java.time.Clock)
    extends Tokens[F] {
  override def create: F[JwtToken] =
    for {
      uuid <- GenUUID[F].make
      claim <- Sync[F].delay(
        JwtClaim(uuid.asJson.noSpaces).issuedNow.expiresIn(exp.value.toMillis)
      )
      secretKey = JwtSecretKey(config.value.value.value)
      token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
    } yield token
}
