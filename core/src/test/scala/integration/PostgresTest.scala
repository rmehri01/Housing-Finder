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
import housingfinder.domain.listings._
import io.estatico.newtype.ops._
import natchez.Trace.Implicits.noop
import skunk.Session
import squants.market.Money
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
      (
          t: Title,
          cs: NonEmptyList[CreateListing],
          m1: Money,
          m2: Money
      ) =>
        spec("Listings") {
          LiveListings.make(pool).flatMap { l =>
            for {
              // multiple listings are added successfully
              x <- l.get()
              _ <- l.addAll(cs.toList)
              a <- l.get()
              csInDb = a.filter(li => cs.exists(_.title == li.title))

              // getting by price ranges works correctly
              lower = m1.min(m2)
              upper = m1.max(m2)
              lowerBound = LowerBound(lower).some
              upperBound = UpperBound(upper).some

              b <- l.get(PriceRange(lowerBound, None))
              d <- l.get(PriceRange(None, upperBound))
              e <- l.get(PriceRange(lowerBound, upperBound))

              // the added listing is updated but id stays the same
              fl = cs.head
              flId = csInDb.head.uuid
              _ <- l.addAll(List(fl.copy(title = t)))
              z <- l.get()
            } yield assert(
              x.isEmpty &&
                csInDb.length === cs.length &&
                b == csInDb.filter(_.price.get >= lower) &&
                d == csInDb.filter(_.price.get <= upper) &&
                e == csInDb
                  .filter(l => l.price.get >= lower && l.price.get <= upper) &&
                z.count(listing =>
                  listing.uuid == flId && listing.title == t
                ) === 1
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
          pw: Password,
          id: ListingId
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

            // try adding a non-existent id
            f <- w.add(d, id).attempt

            // remove listing from watch list
            _ <- w.remove(d, lId)
            z <- w.get(d)
          } yield assert(
            x.isEmpty &&
              y.count(_.uuid == lId) === 1 &&
              e.isLeft &&
              f.isLeft &&
              z.isEmpty
          )
        }
    }
  }
}
