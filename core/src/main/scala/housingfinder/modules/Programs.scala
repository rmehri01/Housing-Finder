package housingfinder.modules

import cats.Parallel
import cats.effect._
import housingfinder.effects._
import housingfinder.programs._
import io.chrisdavenport.log4cats.Logger

object Programs {
  def make[F[_]: Logger: Sync: Timer: Parallel](
      algebras: Algebras[F]
  ): F[Programs[F]] =
    Sync[F].delay(
      new Programs[F](algebras)
    )
}

/** Uses algebras to create the needed programs. */
final class Programs[F[_]: Logger: MonadThrow: Timer: Parallel] private (
    algebras: Algebras[F]
) {
  val updateListings: UpdateListingsProgram[F] = new UpdateListingsProgram[F](
    algebras.listings,
    algebras.scraper
  )
}
