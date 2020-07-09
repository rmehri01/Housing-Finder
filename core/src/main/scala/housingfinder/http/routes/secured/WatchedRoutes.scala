package housingfinder.http.routes.secured

import cats._
import cats.implicits._
import housingfinder.algebras.Watched
import housingfinder.domain.listings.ListingId
import housingfinder.http.auth.users.CommonUser
import housingfinder.http.json._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class WatchedRoutes[F[_]: Defer: JsonDecoder: Monad](
    watched: Watched[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/watched"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user =>
      Ok(watched.getWatched(user.value.id))

    // TODO: maybe error when already in or cannot be found, use .recoverWith on domain errors
    case POST -> Root / UUIDVar(uuid) as user =>
      watched.add(user.value.id, ListingId(uuid)) *>
        Created()

    case DELETE -> Root / UUIDVar(uuid) as user =>
      watched.remove(user.value.id, ListingId(uuid)) *>
        NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
