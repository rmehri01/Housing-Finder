package housingfinder.http.routes.admin

import cats.data.NonEmptyList
import cats.effect.IO
import eu.timepit.refined.api.Refined
import housingfinder.algebras.{Listings, Scraper}
import housingfinder.domain.listings._
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
            new UpdateListingsProgram(new TestListings, new TestScraper)
          ).routes(adminUserMiddleware)
        assertHttpStatus(routes, req)(Status.Ok)
      }
  }
}

protected class TestListings extends Listings[IO] {
  override def get: IO[List[Listing]] = IO.pure(List.empty)

  override def addAll(createListings: List[CreateListing]): IO[Unit] =
    IO.unit
}

protected class TestScraper extends Scraper[IO] {
  override def run: IO[List[CreateListing]] = IO.pure(List.empty)
}
