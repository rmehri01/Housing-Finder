package housingfinder.http.routes.admin

import cats._
import cats.implicits._
import housingfinder.algebras.Listings
import housingfinder.domain.listings.CreateListingParam
import housingfinder.effects.MonadThrow
import housingfinder.http.auth.users.AdminUser
import housingfinder.http.decoder._
import housingfinder.http.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class AdminListingRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    listings: Listings[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/listings"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {

    case ar @ POST -> Root as _ =>
      ar.req.decodeR[List[CreateListingParam]] { cs =>
        listings.addAll(cs.map(_.toDomain)) *>
          Created()
      }

  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )

}
