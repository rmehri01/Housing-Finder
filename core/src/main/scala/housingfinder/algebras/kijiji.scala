package housingfinder.algebras

import housingfinder.domain.kijiji.Listing

trait Kijiji[F[_]] {
  // TODO: some way to filter out listings by desired properties
  def findListings: F[List[Listing]]
  def updateListings: F[Unit]
}
