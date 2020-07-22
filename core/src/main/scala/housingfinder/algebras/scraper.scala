package housingfinder.algebras

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import housingfinder.domain.listings._
import housingfinder.domain.scraper._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

/** Provides a way to extract listing information when given HTML.
  *
  * This is a bit coupled with the client that gets the HTML but this client gives more control over
  * the level of parallelism than the JsoupBrowser does.
  */
trait Scraper[F[_]] {

  /** Returns a list of relative links to the listings found in the HTML. */
  def getUrlsOnPage(html: Html): F[List[RelListingUrl]]

  /** Constructs a single CreateListing using the page HTML and its url. */
  def createListingFromPage(html: Html, url: ListingUrl): F[CreateListing]

}

object LiveScraper {
  def make[F[_]: Sync]: F[LiveScraper[F]] =
    Sync[F].delay {
      new LiveScraper[F]
    }
}

final class LiveScraper[F[_]: Applicative] extends Scraper[F] {
  private val browser = JsoupBrowser()

  override def getUrlsOnPage(html: Html): F[List[RelListingUrl]] =
    (browser.parseString(html.value) >> attrs("href")(".title .title"))
      .map(RelListingUrl.apply)
      .toList
      .pure[F]

  override def createListingFromPage(
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
