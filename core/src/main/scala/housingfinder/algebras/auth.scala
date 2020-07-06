package housingfinder.algebras

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Functor}
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import housingfinder.config.data.TokenExpiration
import housingfinder.domain.auth._
import housingfinder.effects.MonadThrow
import housingfinder.http.auth.users.{AdminUser, CommonUser, User}
import housingfinder.http.json._
import io.circe.parser.decode
import io.circe.syntax._
import pdi.jwt.JwtClaim

trait Auth[F[_]] {
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object LiveAdminAuth {
  def make[F[_]: Sync](
      adminToken: JwtToken,
      adminUser: AdminUser
  ): F[UsersAuth[F, AdminUser]] =
    Sync[F].delay {
      new LiveAdminAuth(adminToken, adminUser)
    }
}

final class LiveAdminAuth[F[_]: Applicative] private (
    adminToken: JwtToken,
    adminUser: AdminUser
) extends UsersAuth[F, AdminUser] {

  override def findUser(
      token: JwtToken
  )(claim: JwtClaim): F[Option[AdminUser]] =
    (token == adminToken).guard[Option].as(adminUser).pure[F]

}

object LiveUsersAuth {
  def make[F[_]: Sync](
      redis: RedisCommands[F, String, String]
  ): F[UsersAuth[F, CommonUser]] =
    Sync[F].delay {
      new LiveUsersAuth(redis)
    }
}

final class LiveUsersAuth[F[_]: Functor] private (
    redis: RedisCommands[F, String, String]
) extends UsersAuth[F, CommonUser] {

  override def findUser(
      token: JwtToken
  )(claim: JwtClaim): F[Option[CommonUser]] =
    redis
      .get(token.value)
      .map(_.flatMap { u =>
        decode[User](u).toOption.map(CommonUser.apply)
      })

}

object LiveAuth {
  def make[F[_]: Sync](
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      users: Users[F],
      redis: RedisCommands[F, String, String]
  ): F[Auth[F]] =
    Sync[F].delay(
      new LiveAuth(tokenExpiration, tokens, users, redis)
    )
}

final class LiveAuth[F[_]: MonadThrow] private (
    tokenExpiration: TokenExpiration,
    tokens: Tokens[F],
    users: Users[F],
    redis: RedisCommands[F, String, String]
) extends Auth[F] {
  private val tokenExpiryDuration = tokenExpiration.value

  override def newUser(username: UserName, password: Password): F[JwtToken] =
    users.find(username, password).flatMap {
      case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
      case None =>
        for {
          i <- users.create(username, password)
          t <- tokens.create
          u = User(i, username).asJson.noSpaces
          _ <- redis.setEx(t.value, u, tokenExpiryDuration)
          _ <- redis.setEx(username.value, t.value, tokenExpiryDuration)
        } yield t
    }

  override def login(username: UserName, password: Password): F[JwtToken] =
    users.find(username, password).flatMap {
      case None => InvalidUserOrPassword(username).raiseError[F, JwtToken]
      case Some(user) =>
        redis.get(username.value).flatMap {
          case Some(t) => JwtToken(t).pure[F]
          case None =>
            tokens.create.flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, tokenExpiryDuration) *>
                redis.setEx(username.value, t.value, tokenExpiryDuration)
            }
        }
    }

  override def logout(token: JwtToken, username: UserName): F[Unit] =
    redis.del(token.value) *>
      redis.del(username.value)
}