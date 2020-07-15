package housingfinder.http.clients

import cats.effect.Concurrent
import cats.implicits._
import housingfinder.domain.scraper._
import housingfinder.effects.BracketThrow
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.middleware.FollowRedirect

trait KijijiClient[F[_]] {
  // TODO: refined types?
  def getHtml(url: String): F[Html]
}

final class LiveKijijiClient[F[_]: JsonDecoder: BracketThrow: Concurrent](
    client: Client[F]
) extends KijijiClient[F] {

  // TODO: not sure where to put
  private implicit val htmlDecoder: EntityDecoder[F, Html] =
    EntityDecoder.text[F].map(Html.apply)

  private val followRedirectClient: Client[F] =
    FollowRedirect(1)(client)

  override def getHtml(url: String): F[Html] =
    Uri.fromString(url).liftTo[F].flatMap { uri =>
      followRedirectClient.get(uri) { r =>
        if (r.status == Status.Ok)
          r.as[Html]
        else
          KijijiConnectionError(
            Option(r.status.reason).getOrElse("unknown")
          ).raiseError[F, Html]
      }
    }

}
