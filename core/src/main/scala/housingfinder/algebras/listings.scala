package housingfinder.algebras

import cats.effect._
import cats.implicits._
import housingfinder.domain.listings._
import housingfinder.effects.GenUUID
import housingfinder.ext.skunkx._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market._

trait Listings[F[_]] {
  // TODO: some way to filter out listings by desired properties
  def get: F[List[Listing]]
  def update: F[Unit]
  def add(createListing: CreateListing): F[Unit]
}

object LiveListings {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Listings[F]] =
    Sync[F].delay(new LiveListings[F](sessionPool))
}

final class LiveListings[F[_]: Sync] private (
    sessionPool: Resource[F, Session[F]]
) extends Listings[F] {
  import ListingQueries._

  override def get: F[List[Listing]] =
    sessionPool.use(_.execute(selectAll))

  // TODO: scrape and for each, add listing
  override def update: F[Unit] = ???

  override def add(listing: CreateListing): F[Unit] =
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

private object ListingQueries {
  val codec: Codec[Listing] =
    (uuid.cimap[ListingId] ~ varchar.cimap[Title] ~ varchar
      .cimap[Address] ~ numeric.imap(CAD.apply)(_.amount) ~ varchar
      .cimap[Description] ~ timestamp).imap {
      case i ~ t ~ a ~ p ~ de ~ da => Listing(i, t, a, p, de, da)
    }(l =>
      l.uuid ~ l.title ~ l.address ~ l.price ~ l.description ~ l.datePosted
    )

  val selectAll: Query[Void, Listing] =
    sql"""
         SELECT * FROM listings
         ORDER BY date_posted DESC
       """.query(codec)

  val insertListing: Command[Listing] =
    sql"""
         INSERT INTO listings
         VALUES ($codec)
       """.command
}
