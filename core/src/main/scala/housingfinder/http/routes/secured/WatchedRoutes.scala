package housingfinder.http.routes.secured

import cats._
import cats.implicits._
import housingfinder.algebras.Watched
import housingfinder.domain.listings.ListingId
import housingfinder.domain.watched.AlreadyWatched
import housingfinder.effects.MonadThrow
import housingfinder.http.auth.users.CommonUser
import housingfinder.http.json._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class WatchedRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    watched: Watched[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/watched"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user =>
      Ok(watched.get(user.value.id))

    case POST -> Root / UUIDVar(uuid) as user =>
      watched.add(user.value.id, ListingId(uuid))
        .flatMap(Created(_))
        .recoverWith {
          case AlreadyWatched(id) => Conflict(id)
        }

    case DELETE -> Root / UUIDVar(uuid) as user =>
      watched.remove(user.value.id, ListingId(uuid)) *>
        NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
