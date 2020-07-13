package housingfinder.algebras

import java.time.LocalDateTime

import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.domain.listings._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

trait Scraper[F[_]] {
  def run: F[List[CreateListing]]
}

// TODO: add refined types
final class LiveScraper[F[_]: Monad: Parallel] extends Scraper[F] {
  private val browser = JsoupBrowser()

  private val BaseUrl =
    "https://www.kijiji.ca"

//  private val StartingRelativeUrl =
//    "/b-apartments-condos/ubc-university-british-columbia/ubc/k0c37l1700291?radius=12.0&address=University+Endowment+Lands%2C+BC&ll=49.273128,-123.248764"

  override def run: F[List[CreateListing]] = ???

  def getListingsOnPage(url: String): F[Set[CreateListing]] =
    (browser.get(url) >> attrs("href")(".title .title")).toList
      .parTraverse { u =>
        val doc = browser.get(BaseUrl + u)

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
          ListingUrl(BaseUrl + u)
        ).pure[F]
      }
      .map(_.toSet)
}
