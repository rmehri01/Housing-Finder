package housingfinder.http.clients

import cats.effect.Concurrent
import cats.implicits._
import housingfinder.domain.scraper._
import housingfinder.effects.BracketThrow
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.client.middleware.FollowRedirect

trait KijijiClient[F[_]] {
  def getHtml(url: String): F[Html]
}

final class LiveKijijiClient[F[_]: JsonDecoder: BracketThrow: Concurrent](
    client: Client[F]
) extends KijijiClient[F]
    with Http4sClientDsl[F] {

  override def getHtml(url: String): F[Html] =
    Uri.fromString(url).liftTo[F].flatMap { uri =>
      followRedirectClient.get(uri) { r =>
        if (r.status == Status.Ok)
          r.as[String].map(Html.apply)
        else
          KijijiConnectionError(
            ErrorMessage(Option(r.status.reason).getOrElse("unknown"))
          ).raiseError[F, Html]
      }
    }

  private val followRedirectClient: Client[F] =
    FollowRedirect(1)(client)

}
