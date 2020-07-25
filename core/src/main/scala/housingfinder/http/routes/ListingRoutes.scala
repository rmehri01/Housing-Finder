package housingfinder.http.routes

import cats._
import housingfinder.algebras.Listings
import housingfinder.domain.listings.{LowerBound, PriceRange, UpperBound}
import housingfinder.http.json._
import housingfinder.http.params._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class ListingRoutes[F[_]: Defer: Monad](
    listings: Listings[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/listings"

  object LowerBoundParam
      extends OptionalQueryParamDecoderMatcher[LowerBound]("lowerPrice")
  object UpperBoundParam
      extends OptionalQueryParamDecoderMatcher[UpperBound]("upperPrice")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? LowerBoundParam(lower) :? UpperBoundParam(upper) =>
      Ok(listings.get(PriceRange(lower, upper)))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
