package housingfinder.http.routes

import cats.effect.Sync
import housingfinder.algebras.HealthCheck
import housingfinder.http.json._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class HealthRoutes[F[_]: Sync](
    healthCheck: HealthCheck[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/healthcheck"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root =>
      Ok(healthCheck.status)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
