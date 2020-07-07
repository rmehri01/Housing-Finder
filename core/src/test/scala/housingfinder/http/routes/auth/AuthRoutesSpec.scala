package housingfinder.http.routes.auth

import cats.effect.IO
import dev.profunktor.auth.jwt.JwtToken
import housingfinder.algebras.Auth
import housingfinder.arbitraries._
import housingfinder.domain.auth._
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite

class AuthRoutesSpec extends AuthHttpTestSuite {

  forAll { (u: Username, p: Password, t: JwtToken) =>
    spec("POST create new user [OK]") {
      POST(
        Map("username" -> u.value, "password" -> p.value),
        uri"/auth" / "users"
      )
        .flatMap { req =>
          val routes =
            new UserRoutes(dataAuth(t)).routes
          assertHttp(routes, req)(Status.Created, t)
        }
    }
  }

  forAll { (u: Username, p: Password, t: JwtToken) =>
    spec("POST login existing user [OK]") {
      POST(
        Map("username" -> u.value, "password" -> p.value),
        uri"/auth" / "login"
      )
        .flatMap { req =>
          val routes =
            new LoginRoutes(dataAuth(t)).routes
          assertHttp(routes, req)(Status.Ok, t)
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
