package housingfinder.http.routes.admin

import cats._
import cats.implicits._
import housingfinder.algebras.Kijiji
import housingfinder.http.auth.users.AdminUser
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class AdminKijijiRoutes[F[_]: Defer: Monad](
    kijiji: Kijiji[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/kijiji"

  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {
    case PUT -> Root as _ =>
      kijiji.updateListings *> Ok()
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )

}
