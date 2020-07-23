package housingfinder.programs

import cats.implicits._
import cats.{Monad, Parallel}
import housingfinder.algebras.{Listings, Scraper}
import housingfinder.domain.listings.{CreateListing, ListingUrl}
import housingfinder.domain.scraper.KijijiUrl
import housingfinder.http.clients.KijijiClient

/** Program for getting listings off of a website and then adding them to the database.
  *
  * Uses the client to retrieve the HTML, the scraper to extract listings, and the listings database
  * to add the results for later use.
  *
  * The current implementation both gets and scrapes the pages in parallel.
  */
final class UpdateListingsProgram[F[_]: Monad: Parallel](
    listings: Listings[F],
    scraper: Scraper[F],
    kijiji: KijijiClient[F]
) {

  private val BaseUrl =
    "https://www.kijiji.ca"

  private def makePageUrl(pageNum: Int): KijijiUrl =
    KijijiUrl(
      BaseUrl + s"/b-apartments-condos/ubc-university-british-columbia/ubc/page-$pageNum/k0c37l1700291?radius=12.0&address=University+Endowment+Lands%2C+BC&ll=49.273128,-123.248764"
    )

  private def scrapeSingleListing(url: ListingUrl): F[CreateListing] =
    kijiji.getHtml(url.value).flatMap { html =>
      scraper.createListingFromPage(
        html,
        url
      )
    }

  private def scrapeSinglePage(url: KijijiUrl): F[List[CreateListing]] =
    for {
      html <- kijiji.getHtml(url.value)
      urls <- scraper.getUrlsOnPage(html)
      listings <- urls.parTraverse(relUrl =>
        scrapeSingleListing(ListingUrl(BaseUrl + relUrl.value))
      )
    } yield listings

  /** Scrapes the first three pages of Kijiji listings and adds them to the database. */
  def scrapeAndUpdate: F[Unit] =
    (1 to 3)
      .map(makePageUrl)
      .toList
      .parFlatTraverse(scrapeSinglePage)
      .map(_.distinct)
      .flatMap(listings.addAll)

}
