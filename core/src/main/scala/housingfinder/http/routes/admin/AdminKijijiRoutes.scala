package housingfinder.http.routes.admin

import cats._
import cats.implicits._
import housingfinder.algebras.Kijiji
import housingfinder.domain.kijiji.CreateListingParam
import housingfinder.effects.MonadThrow
import housingfinder.http.auth.users.AdminUser
import housingfinder.http.decoder._
import housingfinder.http.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class AdminKijijiRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    kijiji: Kijiji[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/kijiji"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[CreateListingParam] { listing =>
        Created(kijiji.addListing(listing.toDomain))
      }

    case PUT -> Root as _ =>
      kijiji.updateListings *> Ok()
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )

}
