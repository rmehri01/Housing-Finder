package housingfinder.http.routes.auth

import cats.effect.IO
import dev.profunktor.auth.jwt.JwtToken
import housingfinder.algebras.Auth
import housingfinder.domain.auth._
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite
import utilities.arbitraries._

class AuthRoutesSpec extends AuthHttpTestSuite {

  forAll { (c: CreateUserParam, t: JwtToken) =>
    spec("POST create new user [OK]") {
      POST(
        c,
        uri"/auth" / "users"
      )
        .flatMap { req =>
          val routes =
            new UserRoutes(dataAuth(t)).routes
          assertHttp(routes, req)(Status.Created, t)
        }
    }
  }

  forAll { (c: CreateUserParam, t: JwtToken) =>
    spec("POST create new user, username in use [ERROR]") {
      POST(
        c,
        uri"/auth" / "users"
      )
        .flatMap { req =>
          val routes =
            new UserRoutes(failingAuthUsernameInUse(t)).routes
          assertHttp(routes, req)(Status.Conflict, c.username.value.value)
        }
    }
  }

  forAll { (l: LoginUserParam, t: JwtToken) =>
    spec("POST login existing user [OK]") {
      POST(
        l,
        uri"/auth" / "login"
      )
        .flatMap { req =>
          val routes =
            new LoginRoutes(dataAuth(t)).routes
          assertHttp(routes, req)(Status.Ok, t)
        }
    }
  }

  forAll { (l: LoginUserParam, t: JwtToken) =>
    spec("POST login existing user, incorrect login [ERROR]") {
      POST(
        l,
        uri"/auth" / "login"
      )
        .flatMap { req =>
          val routes =
            new LoginRoutes(failingAuthInvalidUserOrPassword(t)).routes
          assertHttpStatus(routes, req)(Status.Forbidden)
        }
    }
  }

  spec("POST logout current user [OK]") {
    POST(uri"/auth" / "logout")
      .flatMap { req =>
        val routes =
          new LogoutRoutes(new TestAuth).routes(authUserMiddleware)
        assertHttpStatus(routes, req)(Status.NoContent)
      }
  }

  def dataAuth(token: JwtToken): Auth[IO] =
    new TestAuth {
      override def newUser(
          username: Username,
          password: Password
      ): IO[JwtToken] =
        IO.pure(token)

      override def login(username: Username, password: Password): IO[JwtToken] =
        IO.pure(token)
    }

  def failingAuthUsernameInUse(token: JwtToken): Auth[IO] =
    new TestAuth {
      override def newUser(
          username: Username,
          password: Password
      ): IO[JwtToken] =
        IO.raiseError(UsernameInUse(username)) *> IO.pure(token)
    }

  def failingAuthInvalidUserOrPassword(token: JwtToken): Auth[IO] =
    new TestAuth {
      override def login(username: Username, password: Password): IO[JwtToken] =
        IO.raiseError(InvalidUserOrPassword(username)) *> IO.pure(token)
    }

}

protected class TestAuth extends Auth[IO] {
  override def newUser(
      username: Username,
      password: Password
  ): IO[JwtToken] = IO.pure(JwtToken(""))

  override def login(
      username: Username,
      password: Password
  ): IO[JwtToken] = IO.pure(JwtToken(""))

  override def logout(token: JwtToken, username: Username): IO[Unit] =
    IO.unit
}
