package housingfinder.algebras

import java.time.LocalDateTime

import cats.effect.Sync
import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.domain.listings._
import housingfinder.http.clients.KijijiClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

trait Scraper[F[_]] {
  def getUrlsOnPage(html: String): F[List[String]]
  def createListingFromPage(html: String, url: String): F[CreateListing]
}

object LiveScraper {
  def make[F[_]: Sync: Parallel]: F[LiveScraper[F]] =
    Sync[F].delay {
      new LiveScraper[F]
    }
}

// TODO: add refined types
final class LiveScraper[F[_]: Monad: Parallel] extends Scraper[F] {
  private val browser = JsoupBrowser()

  def getUrlsOnPage(html: String): F[List[String]] =
    (browser.parseString(html) >> attrs("href")(".title .title")).toList.pure[F]

  def createListingFromPage(html: String, url: String): F[CreateListing] = {
    val doc = browser.parseString(html)

    val title = doc >> text(".title-2323565163")
    val address = doc >> text(".address-3617944557")
    val price = doc >?> attr("content")(".currentPrice-2842943473 span")
    val description = doc >> text(".descriptionContainer-3544745383 div")
    val datePosted = doc >> attr("dateTime")("time")

    CreateListing(
      Title(title),
      Address(address),
      CAD(price.getOrElse("0").toDouble),
      Description(description),
      LocalDateTime.parse(datePosted.init),
      // TODO: not the best, kind of coupled with http
      ListingUrl(url)
    ).pure[F]
  }

}
