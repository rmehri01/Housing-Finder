package housingfinder.http.routes.auth

import cats._
import cats.implicits._
import housingfinder.algebras.Auth
import housingfinder.domain.auth.{CreateUser, UserNameInUse}
import housingfinder.effects.MonadThrow
import housingfinder.http.decoder._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case ar @ POST -> Root / "users" =>
      ar.decodeR[CreateUser] { user =>
        auth
          .newUser(user.username.toDomain, user.password.toDomain)
          .flatMap(Created(_))
          .recoverWith {
            case UserNameInUse(u) =>
              Conflict(u.value)
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
