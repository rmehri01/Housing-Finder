package suite

import java.util.UUID

import cats.data.Kleisli
import cats.effect.IO
import housingfinder.domain.auth.{UserId, UserName}
import housingfinder.http.auth.users.{AdminUser, CommonUser, User}
import io.circe.Encoder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.server.AuthMiddleware
import org.scalatest.Assertion

import scala.util.control.NoStackTrace

class HttpTestSuite extends PureTestSuite {

  case object DummyError extends NoStackTrace

  def assertHttp[A: Encoder](routes: HttpRoutes[IO], req: Request[IO])(
      expectedStatus: Status,
      expectedBody: A
  ): IO[Assertion] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp.asJson.map { json =>
          assert(
            resp.status === expectedStatus && json.dropNullValues === expectedBody.asJson.dropNullValues
          )
        }
      case None => fail("route not found")
    }

  def assertHttpStatus(routes: HttpRoutes[IO], req: Request[IO])(
      expectedStatus: Status
  ): IO[Assertion] =
    routes.run(req).value.map {
      case Some(resp) =>
        assert(resp.status === expectedStatus)
      case None => fail("route not found")
    }

  def assertHttpFailure(
      routes: HttpRoutes[IO],
      req: Request[IO]
  ): IO[Assertion] =
    routes.run(req).value.attempt.map {
      case Left(_)  => assert(true)
      case Right(_) => fail("expected a failure")
    }

}

class AuthHttpTestSuite extends HttpTestSuite {
  val authUser: CommonUser = CommonUser(
    User(UserId(UUID.randomUUID), UserName("user"))
  )

  val authUserMiddleware: AuthMiddleware[IO, CommonUser] = AuthMiddleware(
    Kleisli.pure(authUser)
  )

  val adminUser: AdminUser = AdminUser(
    User(UserId(UUID.randomUUID), UserName("admin"))
  )

  val adminUserMiddleware: AuthMiddleware[IO, AdminUser] = AuthMiddleware(
    Kleisli.pure(adminUser)
  )
}
