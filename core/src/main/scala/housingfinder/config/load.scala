package housingfinder.config

import cats.effect.{Async, ContextShift}
import cats.implicits._
import ciris._
import ciris.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import housingfinder.config.data._
import housingfinder.config.environments.AppEnvironment._
import housingfinder.config.environments._

import scala.concurrent.duration._

object load {

  /** Based on the [[housingfinder.config.environments.AppEnvironment]],
    * load the default settings in a different way.
    */
  def apply[F[_]: Async: ContextShift]: F[AppConfig] =
    env("HF_APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Test =>
          default(
            redisUri = RedisURI("redis://localhost")
          )
        case Prod =>
          default(
            redisUri = RedisURI("redis://10.123.154.176")
          )
      }
      .load[F]

  /** The default configuration for the application based on environment variables. */
  private def default(
      redisUri: RedisURI
  ): ConfigValue[AppConfig] =
    (
      env("HF_JWT_SECRET_KEY").as[NonEmptyString].secret,
      env("HF_JWT_CLAIM").as[NonEmptyString].secret,
      env("HF_ACCESS_TOKEN_SECRET_KEY").as[NonEmptyString].secret,
      env("HF_ADMIN_USER_TOKEN").as[NonEmptyString].secret,
      env("HF_PASSWORD_SALT").as[NonEmptyString].secret
    ).parMapN { (secretKey, claimStr, tokenKey, adminToken, salt) =>
      AppConfig(
        AdminJwtConfig(
          JwtSecretKeyConfig(secretKey),
          JwtClaimConfig(claimStr),
          AdminUserTokenConfig(adminToken)
        ),
        JwtSecretKeyConfig(tokenKey),
        PasswordSalt(salt),
        TokenExpiration(30.minutes),
        HttpClientConfig(
          connectTimeout = 45.seconds,
          requestTimeout = 45.seconds
        ),
        PostgreSQLConfig(
          host = "localhost",
          port = 5432,
          user = "postgres",
          database = "store",
          max = 10
        ),
        RedisConfig(redisUri),
        HttpServerConfig(
          host = "0.0.0.0",
          port = 8080,
          responseHeaderTimeout = 45.seconds
        )
      )
    }
}
