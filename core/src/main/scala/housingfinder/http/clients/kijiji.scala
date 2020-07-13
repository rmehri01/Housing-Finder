package housingfinder.http.clients

import cats.implicits._
import housingfinder.domain.scraper.KijijiConnectionError
import housingfinder.effects.BracketThrow
import org.http4s._
import org.http4s.circe._
import org.http4s.client._

trait KijijiClient[F[_]] {
  // TODO: newtypes
  def getBasePage(n: Int): F[String]
  def getHtml(relativeUrl: String): F[String]
}

final class LiveKijijiClient[F[_]: JsonDecoder: BracketThrow](
    client: Client[F]
) extends KijijiClient[F] {
  // TODO: not sure where these should go
  private val BaseUrl =
    "https://www.kijiji.ca"

  private def makePageUrl(n: Int) =
    BaseUrl + s"/b-apartments-condos/ubc-university-british-columbia/ubc/page-$n/k0c37l1700291?radius=12.0&address=University+Endowment+Lands%2C+BC&ll=49.273128,-123.248764"

  override def getBasePage(n: Int): F[String] =
    getHtml(makePageUrl(n))

  override def getHtml(relativeUrl: String): F[String] =
    Uri.fromString(relativeUrl).liftTo[F].flatMap { uri =>
      client.expectOr(uri) { r =>
        KijijiConnectionError(
          Option(r.status.reason).getOrElse("unknown")
        ).raiseError[F, Throwable]
      }
    }
}
