package housingfinder.algebras

import cats.effect._
import cats.implicits._
import housingfinder.domain.kijiji._
import housingfinder.effects.GenUUID
import housingfinder.ext.skunkx._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market._

trait Kijiji[F[_]] {
  // TODO: some way to filter out listings by desired properties
  def getListings: F[List[Listing]]
  def updateListings: F[Unit]
  def addListing(createListing: CreateListing): F[Unit]
}

object LiveKijiji {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Kijiji[F]] =
    Sync[F].delay(new LiveKijiji[F](sessionPool))
}

final class LiveKijiji[F[_]: Sync] private (
    sessionPool: Resource[F, Session[F]]
) extends Kijiji[F] {
  import KijijiQueries._

  def getListings: F[List[Listing]] =
    sessionPool.use(_.execute(selectAll))

  // TODO: scrape and for each, add listing
  def updateListings: F[Unit] = ???

  def addListing(listing: CreateListing): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertListing).use { cmd =>
        GenUUID[F].make[ListingId].flatMap { id =>
          cmd
            .execute(
              Listing(
                id,
                listing.title,
                listing.address,
                listing.price,
                listing.description,
                listing.dateTime
              )
            )
            .void
        }
      }
    }
}

private object KijijiQueries {
  val codec: Codec[Listing] =
    (uuid.cimap[ListingId] ~ varchar.cimap[Title] ~ varchar
      .cimap[Address] ~ numeric.imap(CAD.apply)(_.amount) ~ varchar
      .cimap[Description] ~ timestamp).imap {
      case i ~ t ~ a ~ p ~ de ~ da => Listing(i, t, a, p, de, da)
    }(k =>
      k.uuid ~ k.title ~ k.address ~ k.price ~ k.description ~ k.datePosted
    )

  val selectAll: Query[Void, Listing] =
    sql"""
         SELECT * FROM listings
       """.query(codec)

  val insertListing: Command[Listing] =
    sql"""
         INSERT INTO listings
         VALUES ($codec)
       """.command
}
