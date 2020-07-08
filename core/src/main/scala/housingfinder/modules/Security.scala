package housingfinder.modules

import cats.effect._
import cats.implicits._
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.RedisCommands
import housingfinder.algebras._
import housingfinder.config.data._
import housingfinder.domain.auth._
import housingfinder.effects._
import housingfinder.http.auth.users._
import io.circe.parser.{decode => jsonDecode}
import pdi.jwt._
import skunk.Session

object Security {
  def make[F[_]: Sync](
      cfg: AppConfig,
      sessionPool: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String]
  ): F[Security[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConfig.value.value.value,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.value.value)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content <-
        ApThrow[F].fromEither(jsonDecode[ClaimContent](adminClaim.content))
      adminUser = AdminUser(User(UserId(content.uuid), Username("admin")))
      tokens <- LiveTokens.make[F](cfg.tokenConfig, cfg.tokenExpiration)
      crypto <- LiveCrypto.make[F](cfg.passwordSalt)
      users <- LiveUsers.make[F](sessionPool, crypto)
      auth <- LiveAuth.make[F](cfg.tokenExpiration, tokens, users, redis)
      adminAuth <- LiveAdminAuth.make[F](adminToken, adminUser)
      usersAuth <- LiveUsersAuth.make[F](redis)
    } yield new Security[F](
      auth,
      adminAuth,
      usersAuth,
      adminJwtAuth,
      userJwtAuth
    )

  }
}

final class Security[F[_]] private (
    val auth: Auth[F],
    val adminAuth: UsersAuth[F, AdminUser],
    val usersAuth: UsersAuth[F, CommonUser],
    val adminJwtAuth: AdminJwtAuth,
    val userJwtAuth: UserJwtAuth
)
