package housingfinder.algebras

import housingfinder.domain.auth.UserId
import housingfinder.domain.kijiji._

trait Watched[F[_]] {
  def getWatched: F[List[Listing]]
  def add(userId: UserId, listingId: ListingId): F[Unit]
  def remove(userId: UserId, listingId: ListingId): F[Unit]
}
