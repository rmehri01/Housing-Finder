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
      kijiji <- LiveKijiji.make[F](sessionPool)
      watched <- LiveWatched.make[F](sessionPool)
      health <- LiveHealthCheck.make[F](sessionPool, redis)
    } yield new Algebras[F](kijiji, watched, health)
}

final class Algebras[F[_]] private (
    val kijiji: Kijiji[F],
    val watched: Watched[F],
    val healthCheck: HealthCheck[F]
)
