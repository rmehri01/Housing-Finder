package housingfinder.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import housingfinder.algebras._
import skunk._

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
      redis: RedisCommands[F, String, String],
      sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] =
    for {
      listings <- LiveListings.make[F](sessionPool)
      scraper <- LiveScraper.make[F]
      watched <- LiveWatched.make[F](sessionPool)
      health <- LiveHealthCheck.make[F](sessionPool, redis)
    } yield new Algebras[F](listings, scraper, watched, health)
}

final class Algebras[F[_]] private (
    val listings: Listings[F],
    val scraper: Scraper[F],
    val watched: Watched[F],
    val healthCheck: HealthCheck[F]
)
