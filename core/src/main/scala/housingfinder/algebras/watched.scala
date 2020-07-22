package housingfinder.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import housingfinder.domain.auth.UserId
import housingfinder.domain.listings._
import housingfinder.domain.watched.AlreadyWatched
import housingfinder.effects.BracketThrow
import housingfinder.ext.skunkx._
import skunk._
import skunk.codec.all._
import skunk.implicits._

/** Provides a way for users to watch certain listings. */
trait Watched[F[_]] {
  def get(userId: UserId): F[List[Listing]]
  def add(userId: UserId, listingId: ListingId): F[Unit]
  def remove(userId: UserId, listingId: ListingId): F[Unit]
}

object LiveWatched {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Watched[F]] =
    Sync[F].delay {
      new LiveWatched(sessionPool)
    }
}

final class LiveWatched[F[_]: BracketThrow: Sync] private (
    sessionPool: Resource[F, Session[F]]
) extends Watched[F] {
  import WatchedQueries._

  override def get(userId: UserId): F[List[Listing]] =
    sessionPool.use { session =>
      session.prepare(selectAll).use { q =>
        q.stream(userId, 64).compile.toList
      }
    }

  override def add(userId: UserId, listingId: ListingId): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertWatched).use { cmd =>
        cmd
          .execute(userId ~ listingId)
          .void
          .handleErrorWith {
            case SqlState.UniqueViolation(_) =>
              AlreadyWatched(listingId).raiseError[F, Unit]
          }
      }
    }

  override def remove(userId: UserId, listingId: ListingId): F[Unit] =
    sessionPool.use { session =>
      session.prepare(deleteWatched).use { cmd =>
        cmd.execute(userId ~ listingId).void
      }
    }
}

object WatchedQueries {

  val encoder: Encoder[UserId ~ ListingId] = (uuid ~ uuid).contramap {
    case u ~ l => u.value ~ l.value
  }

  import ListingQueries.codec
  val selectAll: Query[UserId, Listing] =
    sql"""
        SELECT l.*
        FROM watched w
        INNER JOIN listings l ON w.listing_id = l.uuid
        WHERE w.user_id = ${uuid.cimap[UserId]}
       """.query(codec)

  val insertWatched: Command[UserId ~ ListingId] =
    sql"""
        INSERT INTO watched
        VALUES ($encoder)
       """.command

  val deleteWatched: Command[UserId ~ ListingId] =
    sql"""
        DELETE FROM watched 
        WHERE user_id = (${uuid.cimap[UserId]})
        AND listing_id = (${uuid.cimap[ListingId]})
       """.command

}
