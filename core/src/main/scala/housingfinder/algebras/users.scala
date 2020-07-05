package housingfinder.algebras

import housingfinder.domain.auth._
import housingfinder.http.auth.users.User

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}
