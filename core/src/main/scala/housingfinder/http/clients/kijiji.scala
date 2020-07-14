package housingfinder.http.clients

import cats.implicits._
import housingfinder.domain.scraper._
import housingfinder.effects.BracketThrow
import org.http4s._
import org.http4s.circe._
import org.http4s.client._

trait KijijiClient[F[_]] {
  // TODO: newtypes
  def getHtml(url: String): F[String]
}

final class LiveKijijiClient[F[_]: JsonDecoder: BracketThrow](
    client: Client[F]
) extends KijijiClient[F] {

  override def getHtml(url: String): F[String] =
    Uri.fromString(url).liftTo[F].flatMap { uri =>
      client.get(uri) { r =>
        if (r.status == Status.Ok)
          r.asJsonDecode[String]
        else
          KijijiConnectionError(
            Option(r.status.reason).getOrElse("unknown")
          ).raiseError[F, String]
      }
    }

}
