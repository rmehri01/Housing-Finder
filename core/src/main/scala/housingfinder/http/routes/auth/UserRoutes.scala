package housingfinder.http.routes.auth

import cats._
import cats.implicits._
import housingfinder.algebras.Auth
import housingfinder.domain.auth.{CreateUserParam, UsernameInUse}
import housingfinder.effects.MonadThrow
import housingfinder.http.decoder._
import housingfinder.http.json._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "users" =>
      req.decodeR[CreateUserParam] { user =>
        auth
          .newUser(user.username.toDomain, user.password.toDomain)
          .flatMap(Created(_))
          .recoverWith {
            case UsernameInUse(u) => Conflict(u.value)
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
