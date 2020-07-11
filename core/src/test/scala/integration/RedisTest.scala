package integration

import java.util.UUID

import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxEq => _, _}
import cats.kernel.Eq
import ciris.Secret
import dev.profunktor.auth.jwt.{JwtAuth, JwtToken, _}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import housingfinder.algebras.{LiveAuth, LiveTokens, LiveUsersAuth, Users}
import housingfinder.config.data.{JwtSecretKeyConfig, TokenExpiration}
import housingfinder.domain.auth.{Password, UserId, Username}
import housingfinder.effects.GenUUID
import housingfinder.http.auth.users.{User, UserJwtAuth}
import pdi.jwt.{JwtAlgorithm, JwtClaim}
import suite.ResourceSuite
import utilities.arbitraries._
import utilities.logger.NoOp

import scala.concurrent.duration._

class RedisTest extends ResourceSuite[RedisCommands[IO, String, String]] {

  override def resources: Resource[IO, RedisCommands[IO, String, String]] =
    for {
      uri <- Resource.liftF(RedisURI.make[IO]("redis://localhost"))
      client <- RedisClient[IO](uri)
      cmd <- Redis[IO].fromClient[String, String](client, RedisCodec.Utf8)
    } yield cmd

  lazy val tokenConfig: JwtSecretKeyConfig = JwtSecretKeyConfig(
    Secret("bar": NonEmptyString)
  )
  lazy val tokenExp: TokenExpiration = TokenExpiration(30.seconds)
  lazy val jwtClaim: JwtClaim = JwtClaim("test")
  lazy val userJwtAuth: UserJwtAuth =
    UserJwtAuth(JwtAuth.hmac("bar", JwtAlgorithm.HS256))

  withResources { redis =>
    forAll(MaxTests) { (un1: Username, un2: Username, pw: Password) =>
      spec("Authentication") {
        for {
          t <- LiveTokens.make[IO](tokenConfig, tokenExp)
          a <- LiveAuth.make(tokenExp, t, new TestUsers(un2), redis)
          u <- LiveUsersAuth.make[IO](redis)
          x <- u.findUser(JwtToken("invalid"))(jwtClaim)
          j <- a.newUser(un1, pw)
          e <- jwtDecode[IO](j, userJwtAuth.value).attempt
          k <- a.login(un2, pw)
          f <- jwtDecode[IO](k, userJwtAuth.value).attempt
          _ <- a.logout(k, un2)
          y <- u.findUser(k)(jwtClaim)
          w <- u.findUser(j)(jwtClaim)
        } yield assert(
          x.isEmpty && e.isRight && f.isRight && y.isEmpty && w.fold(false)(
            _.value.name.value == un1.value
          )
        )
      }
    }
  }

}

protected class TestUsers(un: Username) extends Users[IO] {
  def find(username: Username, password: Password): IO[Option[User]] =
    Eq[String]
      .eqv(username.value, un.value)
      .guard[Option]
      .as(User(UserId(UUID.randomUUID), un))
      .pure[IO]

  def create(username: Username, password: Password): IO[UserId] =
    GenUUID[IO].make[UserId]
}
