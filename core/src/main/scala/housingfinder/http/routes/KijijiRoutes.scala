package housingfinder.http.routes

import cats._
import housingfinder.algebras.Kijiji
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import housingfinder.http.json._

final class KijijiRoutes[F[_]: Defer: Monad](
    kijiji: Kijiji[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/kijiji"

  // TODO: filtering by optional params
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(kijiji.getListings)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
