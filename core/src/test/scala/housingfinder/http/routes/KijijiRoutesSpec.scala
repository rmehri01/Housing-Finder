package housingfinder.http.routes

import cats.effect.IO
import housingfinder.algebras.Kijiji
import housingfinder.arbitraries._
import housingfinder.domain.kijiji
import housingfinder.domain.kijiji.Listing
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.HttpTestSuite

class KijijiRoutesSpec extends HttpTestSuite {

  forAll { (l: List[Listing]) =>
    spec("GET kijiji [OK]") {
      GET(uri"/kijiji").flatMap { req =>
        val routes = new KijijiRoutes[IO](dataKijiji(l)).routes
        assertHttp(routes, req)(Status.Ok, l)
      }
    }
  }

  forAll { (l: List[Listing]) =>
    spec("GET kijiji [ERROR]") {
      GET(uri"/kijiji").flatMap { req =>
        val routes = new KijijiRoutes[IO](failingKijiji(l)).routes
        assertHttpFailure(routes, req)
      }
    }
  }

  def dataKijiji(listings: List[Listing]): Kijiji[IO] =
    new TestKijiji {
      override def getListings: IO[List[Listing]] = IO.pure(listings)
    }

  def failingKijiji(listings: List[Listing]): Kijiji[IO] =
    new TestKijiji {
      override def getListings: IO[List[Listing]] =
        IO.raiseError(DummyError) *> IO.pure(listings)
    }
}

protected class TestKijiji extends Kijiji[IO] {
  override def getListings: IO[List[kijiji.Listing]] = IO.pure(List.empty)

  override def updateListings: IO[Unit] = IO.unit

  override def addListing(createListing: kijiji.CreateListing): IO[Unit] =
    IO.unit
}
