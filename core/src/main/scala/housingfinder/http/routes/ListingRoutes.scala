package housingfinder.http.routes

import cats._
import housingfinder.algebras.Listings
import housingfinder.http.json._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class ListingRoutes[F[_]: Defer: Monad](
    listings: Listings[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/listings"

  // TODO: filtering by optional params
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(listings.get)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
