package housingfinder.http.routes.admin

import java.time.LocalDateTime

import cats.data.NonEmptyList
import cats.effect.IO
import eu.timepit.refined.api.Refined
import housingfinder.algebras.{Listings, Scraper}
import housingfinder.domain.listings._
import housingfinder.domain.scraper._
import housingfinder.http.clients.KijijiClient
import housingfinder.http.json._
import housingfinder.programs.UpdateListingsProgram
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite
import utilities.arbitraries._

class AdminListingRoutesSpec extends AuthHttpTestSuite {
  forAll { (cs: NonEmptyList[CreateListingParam]) =>
    spec("POST create listings [OK]") {
      POST(
        cs,
        uri"/listings"
      )
        .flatMap { req =>
          val routes =
            new AdminListingRoutes(new TestListings).routes(adminUserMiddleware)
          assertHttpStatus(routes, req)(Status.Created)
        }
    }
  }

  forAll { (c: CreateListingParam, s: String) =>
    spec("POST create listings, refined failed [ERROR]") {
      POST(
        List(c.copy(url = ListingUrlParam(Refined.unsafeApply(s)))),
        uri"/listings"
      ).flatMap { req =>
        val routes =
          new AdminListingRoutes(new TestListings).routes(adminUserMiddleware)
        assertHttpStatus(routes, req)(Status.BadRequest)
      }
    }
  }

  forAll { (s: String) =>
    spec("POST create listings, invalid listings [ERROR]") {
      POST(
        s,
        uri"/listings"
      ).flatMap { req =>
        val routes =
          new AdminListingRoutes(new TestListings).routes(adminUserMiddleware)
        assertHttpStatus(routes, req)(Status.UnprocessableEntity)
      }
    }
  }

  spec("PUT update listings [OK]") {
    PUT(uri"/listings")
      .flatMap { req =>
        val routes =
          new AdminUpdateRoutes(
            new UpdateListingsProgram(
              new TestListings,
              new TestScraper,
              new TestKijijiClient
            )
          ).routes(adminUserMiddleware)
        assertHttpStatus(routes, req)(Status.Ok)
      }
  }

  forAll { (errMsg: ErrorMessage) =>
    spec("PUT update listings, connection failed [ERROR]") {
      PUT(uri"/listings")
        .flatMap { req =>
          val routes =
            new AdminUpdateRoutes(
              new UpdateListingsProgram(
                new TestListings,
                new TestScraper,
                failingKijijiClient(errMsg)
              )
            ).routes(adminUserMiddleware)
          assertHttp(routes, req)(Status.InternalServerError, errMsg)
        }
    }
  }

  def failingKijijiClient(errMsg: ErrorMessage): KijijiClient[IO] =
    new TestKijijiClient {
      override def getHtml(url: String): IO[Html] =
        IO.raiseError(KijijiConnectionError(errMsg)) *> IO.pure(Html(""))
    }
}

protected class TestListings extends Listings[IO] {
  override def get(priceRange: PriceRange): IO[List[Listing]] =
    IO.pure(List.empty)

  override def addAll(createListings: List[CreateListing]): IO[Unit] =
    IO.unit
}

protected class TestScraper extends Scraper[IO] {
  override def getUrlsOnPage(html: Html): IO[List[RelListingUrl]] =
    IO.pure(List.empty)

  override def createListingFromPage(
      html: Html,
      url: ListingUrl
  ): IO[CreateListing] =
    IO.pure(
      CreateListing(
        Title(""),
        Address(""),
        None,
        Description(""),
        LocalDateTime.parse("2019-01-21T05:47:20.949"),
        ListingUrl("")
      )
    )
}

protected class TestKijijiClient extends KijijiClient[IO] {
  override def getHtml(url: String): IO[Html] =
    IO.pure(Html(""))
}
