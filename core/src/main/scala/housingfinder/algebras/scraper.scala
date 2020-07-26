package housingfinder.algebras

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import cats.effect.Sync
import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.domain.listings._
import housingfinder.domain.scraper._
import housingfinder.http.clients.KijijiClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

/** Provides a way to extract listing information from websites. */
trait Scraper[F[_]] {
  def run: F[List[CreateListing]]
}

object LiveScraper {
  def make[F[_]: Sync: Parallel](kijiji: KijijiClient[F]): F[LiveScraper[F]] =
    Sync[F].delay {
      new LiveScraper[F](kijiji)
    }
}

// Using the http4s client gives more control over the level of parallelism than the JsoupBrowser does.
final class LiveScraper[F[_]: Monad: Parallel] private (
    kijiji: KijijiClient[F]
) extends Scraper[F] {

  /** Scrapes the first three pages of Kijiji listings and adds them to the database. */
  override def run: F[List[CreateListing]] =
    (1 to 3)
      .map(makePageUrl)
      .toList
      .parFlatTraverse(scrapeSinglePage)
      .map(_.distinct)

  private val BaseUrl =
    "https://www.kijiji.ca"

  private def makePageUrl(pageNum: Int): KijijiUrl =
    KijijiUrl(
      BaseUrl + s"/b-apartments-condos/ubc-university-british-columbia/ubc/page-$pageNum/k0c37l1700291?radius=12.0&address=University+Endowment+Lands%2C+BC&ll=49.273128,-123.248764"
    )

  /** Constructs a list of CreateListings based on a main page with listing urls on it. */
  private def scrapeSinglePage(url: KijijiUrl): F[List[CreateListing]] =
    for {
      html <- kijiji.getHtml(url.value)
      urls <- getUrlsOnPage(html)
      listings <- urls.parTraverse(relUrl =>
        scrapeSingleListing(ListingUrl(BaseUrl + relUrl.value))
      )
    } yield listings

  /** Constructs a CreateListing given it's listing url. */
  private def scrapeSingleListing(url: ListingUrl): F[CreateListing] =
    kijiji.getHtml(url.value).flatMap { html =>
      createListingFromPage(
        html,
        url
      )
    }

  private val browser = JsoupBrowser()

  /** Returns a list of relative links to the listings found in the HTML. */
  private def getUrlsOnPage(html: Html): F[List[RelListingUrl]] =
    (browser.parseString(html.value) >> attrs("href")(".title .title"))
      .map(RelListingUrl.apply)
      .toList
      .pure[F]

  /** Constructs a single CreateListing using the page HTML and its url. */
  private def createListingFromPage(
      html: Html,
      url: ListingUrl
  ): F[CreateListing] = {
    val doc = browser.parseString(html.value)

    val title = doc >> text(".title-2323565163")

    val address = doc >> text(".address-3617944557")

    // Optional since the listing may have another price option like "Please Contact".
    val price = doc >?> attr("content")(".currentPrice-2842943473 span")

    val description = doc >> text(".descriptionContainer-3544745383 div")

    // On the site this is inconsistent, it is either in a time tag or just a span.
    val dateStr =
      (doc >?> attr("title")("time"))
        .getOrElse(doc >> attr("title")(".datePosted-383942873 span"))

    val datePosted = LocalDateTime.parse(
      dateStr,
      DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH)
    )

    CreateListing(
      Title(title),
      Address(address),
      price.map(s => CAD(s.toDouble)),
      Description(description),
      datePosted,
      url
    ).pure[F]
  }

}
