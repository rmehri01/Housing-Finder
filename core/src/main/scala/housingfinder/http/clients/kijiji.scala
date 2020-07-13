package housingfinder.http.clients

import cats.implicits._
import housingfinder.domain.scraper._
import housingfinder.effects.BracketThrow
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import housingfinder.http.json._

trait KijijiClient[F[_]] {
  // TODO: newtypes
  def getHtml(url: String): F[String]
}

final class LiveKijijiClient[F[_]: JsonDecoder: BracketThrow](
    client: Client[F]
) extends KijijiClient[F] {

  override def getHtml(url: String): F[String] =
    Uri.fromString(url).liftTo[F].flatMap { uri =>
      client.expectOr[String](uri) { r =>
        KijijiConnectionError(
          Option(r.status.reason).getOrElse("unknown")
        ).raiseError[F, Throwable]
      }
    }

}
