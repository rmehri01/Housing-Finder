package housingfinder.programs

import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.algebras.{Listings, Scraper}
import housingfinder.domain.listings.CreateListing
import housingfinder.http.clients.KijijiClient

final class UpdateListingsProgram[F[_]: Monad: Parallel](
    listings: Listings[F],
    scraper: Scraper[F],
    kijiji: KijijiClient[F]
) {

  private val BaseUrl =
    "https://www.kijiji.ca"

  private def makePageUrl(pageNum: Int) =
    BaseUrl + s"/b-apartments-condos/ubc-university-british-columbia/ubc/page-$pageNum/k0c37l1700291?radius=12.0&address=University+Endowment+Lands%2C+BC&ll=49.273128,-123.248764"

  private def scrapeSingleListing(url: String): F[CreateListing] =
    kijiji.getHtml(url).flatMap { html =>
      scraper.createListingFromPage(html, url)
    }

  private def scrapeSinglePage(url: String): F[List[CreateListing]] =
    for {
      html <- kijiji.getHtml(url)
      urls <- scraper.getUrlsOnPage(html)
      listings <- urls.parTraverse(scrapeSingleListing)
    } yield listings

  def scrapeAndUpdate: F[Unit] =
    (1 to 3)
      .map(makePageUrl)
      .toList
      .parFlatTraverse(scrapeSinglePage)
      .map(_.distinct)
      .flatMap(listings.addAll)

}
