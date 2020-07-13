package housingfinder.modules

import cats.effect.Sync
import housingfinder.http.clients.{KijijiClient, LiveKijijiClient}
import org.http4s.client.Client

object HttpClients {
  def make[F[_]: Sync](
      client: Client[F]
  ): F[HttpClients[F]] =
    Sync[F].delay(
      new HttpClients[F] {
        def kijiji: KijijiClient[F] = new LiveKijijiClient[F](client)
      }
    )
}

trait HttpClients[F[_]] {
  def kijiji: KijijiClient[F]
}
