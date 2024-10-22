package housingfinder.http.routes

import cats.effect.IO
import housingfinder.algebras.Listings
import housingfinder.domain.listings.{CreateListing, Listing, PriceRange}
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.HttpTestSuite
import utilities.arbitraries._

class ListingRoutesSpec extends HttpTestSuite {

  forAll { (l: List[Listing]) =>
    spec("GET listings [OK]") {
      GET(uri"/listings").flatMap { req =>
        val routes = new ListingRoutes(dataListings(l)).routes
        assertHttp(routes, req)(Status.Ok, l)
      }
    }
  }

  forAll { (l: List[Listing]) =>
    spec("GET listings [ERROR]") {
      GET(uri"/listings").flatMap { req =>
        val routes = new ListingRoutes(failingListings(l)).routes
        assertHttpFailure(routes, req)
      }
    }
  }

  def dataListings(listings: List[Listing]): Listings[IO] =
    new TestListings {
      override def get(priceRange: PriceRange): IO[List[Listing]] =
        IO.pure(listings)
    }

  def failingListings(listings: List[Listing]): Listings[IO] =
    new TestListings {
      override def get(priceRange: PriceRange): IO[List[Listing]] =
        IO.raiseError(DummyError) *> IO.pure(listings)
    }
}

protected class TestListings extends Listings[IO] {
  override def get(priceRange: PriceRange): IO[List[Listing]] =
    IO.pure(List.empty)

  override def addAll(createListings: List[CreateListing]): IO[Unit] =
    IO.unit
}
