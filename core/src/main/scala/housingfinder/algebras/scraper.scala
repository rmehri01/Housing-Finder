package housingfinder.algebras

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats.Parallel
import cats.effect.Sync
import cats.implicits._
import housingfinder.domain.listings._
import housingfinder.domain.scraper._
import housingfinder.effects.MonadThrow
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

trait Scraper[F[_]] {
  def getUrlsOnPage(html: Html): F[List[RelListingUrl]]
  def createListingFromPage(html: Html, url: ListingUrl): F[CreateListing]
}

object LiveScraper {
  def make[F[_]: Sync: Parallel]: F[LiveScraper[F]] =
    Sync[F].delay {
      new LiveScraper[F]
    }
}

final class LiveScraper[F[_]: MonadThrow: Parallel] extends Scraper[F] {
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
    // optional since the listing may have another price option like "Please Contact"
    val price = doc >?> attr("content")(".currentPrice-2842943473 span")
    val description = doc >> text(".descriptionContainer-3544745383 div")
    // on the site this is inconsistent, it is either in a time tag or just a span
    val dateStr =
      (doc >?> attr("title")("time"))
        .getOrElse(doc >> attr("title")(".datePosted-383942873 span"))

    val datePosted = LocalDateTime.parse(
      dateStr,
      DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")
    )

    // TODO: proper error handling, probably using validated
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
