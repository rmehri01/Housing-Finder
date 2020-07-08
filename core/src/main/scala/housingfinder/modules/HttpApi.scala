package housingfinder.modules

import cats.effect._
import cats.implicits._
import dev.profunktor.auth.JwtAuthMiddleware
import housingfinder.http.auth.users._
import housingfinder.http.routes._
import housingfinder.http.routes.admin._
import housingfinder.http.routes.auth._
import housingfinder.http.routes.secured._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._

import scala.concurrent.duration._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
      algebras: Algebras[F],
      security: Security[F]
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        algebras,
        security
      )
    )
}

final class HttpApi[F[_]: Concurrent: Timer] private (
    algebras: Algebras[F],
    security: Security[F]
) {
  private val adminMiddleware =
    JwtAuthMiddleware[F, AdminUser](
      security.adminJwtAuth.value,
      security.adminAuth.findUser
    )
  private val usersMiddleware =
    JwtAuthMiddleware[F, CommonUser](
      security.userJwtAuth.value,
      security.usersAuth.findUser
    )

  // Auth routes
  private val loginRoutes = new LoginRoutes[F](security.auth).routes
  private val logoutRoutes =
    new LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes = new UserRoutes[F](security.auth).routes

  // Open routes
  private val healthRoutes = new HealthRoutes[F](algebras.healthCheck).routes
  private val kijijiRoutes = new KijijiRoutes[F](algebras.kijiji).routes

  // Secured routes
  private val watchedRoutes =
    new WatchedRoutes[F](algebras.watched).routes(usersMiddleware)

  // Admin routes
  private val adminKijijiRoutes =
    new AdminKijijiRoutes[F](algebras.kijiji).routes(adminMiddleware)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    healthRoutes <+> kijijiRoutes <+> loginRoutes <+> userRoutes <+>
      logoutRoutes <+> watchedRoutes

  private val adminRoutes: HttpRoutes[F] = adminKijijiRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes,
    version.v1 + "/admin" -> adminRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}