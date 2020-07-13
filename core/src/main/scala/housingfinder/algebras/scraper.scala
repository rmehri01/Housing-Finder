package housingfinder.algebras

import java.time.LocalDateTime

import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.domain.listings._
import housingfinder.http.clients.KijijiClient
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import squants.market.CAD

trait Scraper[F[_]] {
  def run: F[List[CreateListing]]
}

// TODO: add refined types
final class LiveScraper[F[_]: Monad: Parallel] private (
    kijiji: KijijiClient[F]
) extends Scraper[F] {
  private val browser = JsoupBrowser()

  override def run: F[List[CreateListing]] =
    (1 to 3).toList.parTraverse { i =>
      kijiji
        .getBasePage(i)
        .flatMap { html =>
          getListingsOnPage(html)
        }
        .map(_.toList)
    }

  def getListingsOnPage(html: String): F[Set[CreateListing]] =
    (browser.parseString(html) >> attrs("href")(".title .title")).toList
      .parTraverse { u =>
        kijiji.getHtml(u).flatMap { html =>
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
            ListingUrl("https://www.kijiji.ca" + u)
          ).pure[F]
        }
      }
      .map(_.toSet)
}
