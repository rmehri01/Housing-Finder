package integration

import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxEq => _, _}
import ciris.Secret
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import housingfinder.algebras._
import utilities.arbitraries._
import housingfinder.config.data.PasswordSalt
import housingfinder.domain.auth.{Password, Username}
import housingfinder.domain.listings.CreateListing
import io.estatico.newtype.ops._
import natchez.Trace.Implicits.noop
import skunk.Session
import suite.ResourceSuite

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
    forAll(MaxTests) { (c: CreateListing) =>
      spec("Listings") {
        LiveListings.make(pool).flatMap { l =>
          for {
            x <- l.get
            _ <- l.add(c)
            y <- l.get
            z <- l.add(c).attempt
          } yield assert(
            x.isEmpty &&
              y.count(_.title.value === c.title.value) === 1 &&
              z.isLeft
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
          x <- u.find(username, password)
          y <- u.find(username, "foo".coerce[Password])
          z <- u.create(username, password).attempt
        } yield assert(
          x.count(_.id.value === d.value) === 1 && y.isEmpty && z.isLeft
        )
      }
    }

    // TODO: a bit awkward without being able to get specific listing
    forAll(MaxTests) {
      (
          c: CreateListing,
          un: Username,
          pw: Password
      ) =>
        spec("Watched") {
          for {
            l <- LiveListings.make[IO](pool)
            _ <- l.add(c)
            l <- l.get
            lId = l.head.uuid

            c <- LiveCrypto.make[IO](salt)
            u <- LiveUsers.make[IO](pool, c)
            d <- u.create(un, pw)

            w <- LiveWatched.make[IO](pool)
            x <- w.getWatched(d)
            _ <- w.add(d, lId)
            y <- w.getWatched(d)
            _ <- w.remove(d, lId)
            z <- w.getWatched(d)
          } yield assert(
            x.isEmpty && y.count(_.uuid.value === lId.value) === 1 && z.isEmpty
          )
        }
    }
  }
}
