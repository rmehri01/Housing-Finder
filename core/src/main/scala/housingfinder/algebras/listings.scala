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
  def addAll(createListings: List[CreateListing]): F[Unit]
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

  override def addAll(createListings: List[CreateListing]): F[Unit] =
    sessionPool.use { session =>
      val len = createListings.length

      session.prepare(insertListings(len)).use { cmd =>
        GenUUID[F].make[ListingId].replicateA(len).flatMap { ids =>
          cmd
            .execute(
              (ids, createListings).mapN((id, c) =>
                Listing(
                  id,
                  c.title,
                  c.address,
                  c.price,
                  c.description,
                  c.dateTime,
                  c.listingUrl
                )
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
      .cimap[Description] ~ timestamp ~ varchar.cimap[ListingUrl]).imap {
      case i ~ t ~ a ~ p ~ de ~ da ~ u => Listing(i, t, a, p, de, da, u)
    }(l =>
      l.uuid ~ l.title ~ l.address ~ l.price ~ l.description ~ l.datePosted ~ l.url
    )

  val selectAll: Query[Void, Listing] =
    sql"""
         SELECT * FROM listings
         ORDER BY date_posted DESC
       """.query(codec)

  // if the url is the same, updates the listing instead of creating a new one
  def insertListings(n: Int): Command[List[Listing]] = {
    val listingsCodec = codec.list(n)
    sql"""
        INSERT INTO listings (uuid, title, address, price, description, date_posted, url)
        VALUES ($listingsCodec)
        ON CONFLICT (url) DO UPDATE
            SET title       = EXCLUDED.title,
                address     = EXCLUDED.address,
                description = EXCLUDED.address,
                url         = EXCLUDED.url
       """.command
  }
}
