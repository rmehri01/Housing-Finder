package housingfinder.http.routes.admin

import cats.Defer
import cats.implicits._
import housingfinder.domain.scraper.KijijiConnectionError
import housingfinder.effects.MonadThrow
import housingfinder.http.auth.users.AdminUser
import housingfinder.http.json._
import housingfinder.programs.UpdateListingsProgram
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}

final class AdminUpdateRoutes[F[_]: Defer: MonadThrow](
    program: UpdateListingsProgram[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/listings"

  // TODO: error handling
  private val httpRoutes: AuthedRoutes[AdminUser, F] = AuthedRoutes.of {

    case PUT -> Root as _ =>
      program.scrapeAndUpdate
        .flatMap(_ => Ok())
        .recoverWith {
          case KijijiConnectionError(e) => InternalServerError(e)
        }

  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] =
    Router(
      prefixPath -> authMiddleware(httpRoutes)
    )
}
