package housingfinder.http.routes.secured

import cats.effect.IO
import housingfinder.algebras.Watched
import housingfinder.arbitraries._
import housingfinder.domain.kijiji.{Listing, ListingId}
import housingfinder.domain.{auth, kijiji}
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite

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
      override def getWatched(userId: auth.UserId): IO[List[Listing]] =
        IO.pure(listings)
    }

}

protected class TestWatched extends Watched[IO] {
  override def getWatched(userId: auth.UserId): IO[List[kijiji.Listing]] =
    IO.pure(List.empty)

  override def add(userId: auth.UserId, listingId: kijiji.ListingId): IO[Unit] =
    IO.unit

  override def remove(
      userId: auth.UserId,
      listingId: kijiji.ListingId
  ): IO[Unit] = IO.unit
}
