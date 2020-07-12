package housingfinder.http.routes.secured

import cats.effect.IO
import housingfinder.algebras.Watched
import housingfinder.domain.listings.{Listing, ListingId}
import housingfinder.domain.watched.AlreadyWatched
import housingfinder.domain.{auth, listings}
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite
import utilities.arbitraries._

class WatchedRoutesSpec extends AuthHttpTestSuite {

  forAll { (l: List[Listing]) =>
    spec("GET watched [OK]") {
      GET(uri"/watched").flatMap { req =>
        val routes =
          new WatchedRoutes(dataWatched(l)).routes(authUserMiddleware)
        assertHttp(routes, req)(Status.Ok, l)
      }
    }
  }

  forAll { (l: ListingId) =>
    spec("POST add listing to watched list [OK]") {
      POST(uri"/watched" / l.value.toString).flatMap { req =>
        val routes =
          new WatchedRoutes(new TestWatched).routes(authUserMiddleware)
        assertHttpStatus(routes, req)(Status.Created)
      }
    }
  }

  forAll { (l: ListingId) =>
    spec("POST add listing, already watched [ERROR]") {
      POST(uri"/watched" / l.value.toString).flatMap { req =>
        val routes = new WatchedRoutes(failingWatchedAlreadyAdded).routes(
          authUserMiddleware
        )
        assertHttp(routes, req)(Status.Conflict, l)
      }
    }
  }

  forAll { (l: ListingId) =>
    spec("DELETE remove listing from watched list [OK]") {
      DELETE(uri"/watched" / l.value.toString).flatMap { req =>
        val routes =
          new WatchedRoutes(new TestWatched).routes(authUserMiddleware)
        assertHttpStatus(routes, req)(Status.NoContent)
      }
    }
  }

  def dataWatched(listings: List[Listing]): Watched[IO] =
    new TestWatched {
      override def get(userId: auth.UserId): IO[List[Listing]] =
        IO.pure(listings)
    }

  def failingWatchedAlreadyAdded: Watched[IO] =
    new TestWatched {
      override def add(userId: auth.UserId, listingId: ListingId): IO[Unit] =
        IO.raiseError(AlreadyWatched(listingId)) *> IO.unit
    }
}

protected class TestWatched extends Watched[IO] {
  override def get(userId: auth.UserId): IO[List[listings.Listing]] =
    IO.pure(List.empty)

  override def add(
      userId: auth.UserId,
      listingId: listings.ListingId
  ): IO[Unit] =
    IO.unit

  override def remove(
      userId: auth.UserId,
      listingId: listings.ListingId
  ): IO[Unit] = IO.unit
}
