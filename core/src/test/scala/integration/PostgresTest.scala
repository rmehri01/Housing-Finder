package integration

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxEq => _, _}
import ciris.Secret
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import housingfinder.algebras._
import housingfinder.config.data.PasswordSalt
import housingfinder.domain.auth.{Password, Username}
import housingfinder.domain.listings.{CreateListing, Title}
import io.estatico.newtype.ops._
import natchez.Trace.Implicits.noop
import skunk.Session
import suite.ResourceSuite
import utilities.arbitraries._

class PostgresTest extends ResourceSuite[Resource[IO, Session[IO]]] {

  override def resources: Resource[IO, Resource[IO, Session[IO]]] =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "store",
      max = 10
    )

  withResources { pool =>
    forAll(MaxTests) {
      (c: CreateListing, t: Title, cs: NonEmptyList[CreateListing]) =>
        spec("Listings") {
          LiveListings.make(pool).flatMap { l =>
            for {
              // single listing is added successfully
              x <- l.get()
              _ <- l.addAll(List(c))
              y <- l.get()

              // the added listing is updated but id stays the same
              yId = y.find(_.title == c.title).get.uuid
              _ <- l.addAll(List(c.copy(title = t)))
              z <- l.get()

              // multiple listings are added successfully
              _ <- l.addAll(cs.toList)
              a <- l.get()
            } yield assert(
              !x.exists(_.title == c.title) &&
                y.count(_.title == c.title) === 1 &&
                z.count(listing =>
                  listing.uuid == yId && listing.title == t
                ) === 1 &&
                a.count(li => cs.exists(_.title == li.title)) === cs.length
            )
          }
        }
    }

    lazy val salt = Secret("secret": NonEmptyString).coerce[PasswordSalt]

    forAll(MaxTests) { (username: Username, password: Password) =>
      spec("Users") {
        for {
          c <- LiveCrypto.make[IO](salt)
          u <- LiveUsers.make[IO](pool, c)
          d <- u.create(username, password)

          // get user by correct username and password
          x <- u.find(username, password)

          // incorrect password should fail
          y <- u.find(username, "foo".coerce[Password])

          // cannot make a duplicate user
          z <- u.create(username, password).attempt
        } yield assert(
          x.count(_.id == d) === 1 && y.isEmpty && z.isLeft
        )
      }
    }

    forAll(MaxTests) {
      (
          c: CreateListing,
          un: Username,
          pw: Password
      ) =>
        spec("Watched") {
          for {
            // add a listing to be used later
            l <- LiveListings.make[IO](pool)
            _ <- l.addAll(List(c))
            l <- l.get()
            lId = l.find(_.title == c.title).get.uuid

            // create a user
            c <- LiveCrypto.make[IO](salt)
            u <- LiveUsers.make[IO](pool, c)
            d <- u.create(un, pw)

            // add a single listing to the watch list
            w <- LiveWatched.make[IO](pool)
            x <- w.get(d)
            _ <- w.add(d, lId)
            y <- w.get(d)

            // try to add a duplicate to the watch list
            e <- w.add(d, lId).attempt

            // remove listing from watch list
            _ <- w.remove(d, lId)
            z <- w.get(d)
          } yield assert(
            x.isEmpty && y.count(_.uuid == lId) === 1 && e.isLeft && z.isEmpty
          )
        }
    }
  }
}
