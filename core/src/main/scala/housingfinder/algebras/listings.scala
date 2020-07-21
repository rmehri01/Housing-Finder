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
  def get(priceRange: PriceRange = PriceRange(None, None)): F[List[Listing]]
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

  override def get(priceRange: PriceRange): F[List[Listing]] =
    sessionPool.use { session =>
      session.execute(createSelectListingsFunction).void *> session
        .prepare(selectListings)
        .use { q =>
          q.stream(priceRange.lower ~ priceRange.upper, 64).compile.toList
        }
    }

  override def addAll(createListings: List[CreateListing]): F[Unit] =
    sessionPool.use { session =>
      GenUUID[F].make[ListingId].replicateA(createListings.length).flatMap {
        ids =>
          val ls = ids.zip(createListings).map {
            case (id, c) =>
              Listing(
                id,
                c.title,
                c.address,
                c.price,
                c.description,
                c.dateTime,
                c.listingUrl
              )
          }

          session.prepare(insertListings(ls)).use { cmd =>
            cmd.execute(ls).void
          }

      }
    }
}

private object ListingQueries {
  val idCodec: Codec[ListingId] = uuid.cimap[ListingId]
  val titleCodec: Codec[Title] = varchar.cimap[Title]
  val addressCodec: Codec[Address] = varchar.cimap[Address]

  val moneyCodec: Codec[Money] = numeric.imap(CAD.apply)(_.amount)
  val optMoneyCodec: Codec[Option[Money]] = moneyCodec.opt

  val descriptionCodec: Codec[Description] = varchar.cimap[Description]
  val urlCodec: Codec[ListingUrl] = varchar.cimap[ListingUrl]

  val codec: Codec[Listing] =
    (idCodec ~ titleCodec ~ addressCodec ~ optMoneyCodec ~ descriptionCodec ~ timestamp ~ urlCodec)
      .imap {
        case i ~ t ~ a ~ p ~ de ~ da ~ u => Listing(i, t, a, p, de, da, u)
      }(l =>
        l.uuid ~ l.title ~ l.address ~ l.price ~ l.description ~ l.datePosted ~ l.url
      )

  val createSelectListingsFunction: Command[Void] =
    sql"""
        CREATE OR REPLACE FUNCTION select_listings(lower NUMERIC, upper NUMERIC)
            RETURNS SETOF listings
            LANGUAGE plpgsql AS
        '
            BEGIN
                RETURN QUERY
                    SELECT *
                    FROM listings
                    WHERE (lower IS NULL OR lower <= price)
                      AND (upper IS NULL OR upper >= price)
                    ORDER BY date_posted DESC;
            END
        '
      """.command

  val selectListings: Query[Option[LowerBound] ~ Option[UpperBound], Listing] =
    sql"""
        SELECT *
        FROM select_listings(${moneyCodec.cimap[LowerBound].opt},
            ${moneyCodec.cimap[UpperBound].opt})
       """.query(codec)

  // if the url is the same, updates the listing instead of creating a new one
  def insertListings(ls: List[Listing]): Command[ls.type] = {
    val listingsCodec = codec.values.list(ls)
    sql"""
        INSERT INTO listings (uuid, title, address, price, description, date_posted, url)
        VALUES $listingsCodec
        ON CONFLICT (url) DO UPDATE
            SET title       = EXCLUDED.title,
                address     = EXCLUDED.address,
                price       = EXCLUDED.price,
                description = EXCLUDED.address,
                date_posted = EXCLUDED.date_posted,
                url         = EXCLUDED.url
       """.command
  }
}
