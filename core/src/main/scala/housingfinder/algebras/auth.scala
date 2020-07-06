package housingfinder.algebras

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Functor}
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import housingfinder.domain.auth._
import housingfinder.http.auth.users.{AdminUser, CommonUser, User}
import housingfinder.http.json._
import io.circe.parser.decode
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
