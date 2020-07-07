package integration

import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxEq => _, _}
import housingfinder.algebras.LiveKijiji
import housingfinder.arbitraries._
import housingfinder.domain.kijiji.CreateListing
import natchez.Trace.Implicits.noop // needed for skunk
import skunk.Session
import suite.ResourceSuite

class PostgresTest extends ResourceSuite[Resource[IO, Session[IO]]] {

  // For it:tests, one test is enough since it's fairly expensive
  val MaxTests: PropertyCheckConfigParam = MinSuccessful(1)

  override def resources: Resource[IO, Resource[IO, Session[IO]]] =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "ryanmehri",
      database = "housingfinder",
      max = 10
    )

  withResources { pool =>
    forAll(MaxTests) { (c: CreateListing) =>
      spec("Kijiji") {
        LiveKijiji.make(pool).flatMap { k =>
          for {
            x <- k.getListings
            _ <- k.addListing(c)
            y <- k.getListings
            z <- k.addListing(c).attempt
          } yield assert(
            x.isEmpty && y
              .count(_.title.value === c.title.value) === 1 && z.isLeft
          )
        }
      }
    }
  }
}
