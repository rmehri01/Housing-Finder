package housingfinder.http.routes.admin

import cats.effect.IO
import housingfinder.algebras.Kijiji
import housingfinder.arbitraries._
import housingfinder.domain.kijiji._
import housingfinder.http.json._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.AuthHttpTestSuite

class AdminKijijiRoutesSpec extends AuthHttpTestSuite {
  forAll { (c: CreateListingParam) =>
    spec("POST create listing [OK]") {
      POST(
        c,
        uri"/kijiji"
      )
        .flatMap { req =>
          println(c)
          val routes =
            new AdminKijijiRoutes(new TestKijiji).routes(adminUserMiddleware)
          assertHttpStatus(routes, req)(Status.Created)
        }
    }
  }

  spec("PUT update listings [OK]") {
    PUT(uri"/kijiji")
      .flatMap { req =>
        val routes =
          new AdminKijijiRoutes(new TestKijiji).routes(adminUserMiddleware)
        assertHttpStatus(routes, req)(Status.Ok)
      }
  }
}

protected class TestKijiji extends Kijiji[IO] {
  override def getListings: IO[List[Listing]] = IO.pure(List.empty)

  override def updateListings: IO[Unit] = IO.unit

  override def addListing(createListing: CreateListing): IO[Unit] =
    IO.unit
}
