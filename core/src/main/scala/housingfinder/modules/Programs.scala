package housingfinder.modules

import cats.effect._
import cats.implicits._
import housingfinder.algebras.LiveScraper
import housingfinder.effects._
import housingfinder.programs._
import io.chrisdavenport.log4cats.Logger

object Programs {
  def make[F[_]: Logger: Sync: Timer](
      algebras: Algebras[F],
      clients: HttpClients[F]
  ): F[Programs[F]] =
    Sync[F].delay(
      new Programs[F](algebras, clients)
    )
}

final class Programs[F[_]: Logger: MonadThrow: Timer] private (
    algebras: Algebras[F],
    clients: HttpClients[F]
) {

  val updateListings: UpdateListingsProgram[F] = new UpdateListingsProgram[F](
    algebras.listings,
    LiveScraper.make,
    clients.kijiji
  )

}
