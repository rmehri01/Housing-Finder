package housingfinder.effects

import java.util.UUID

import cats.effect.Sync
import cats.implicits._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

/** Deals with the generation of UUIDs that may be coerced into newtypes. */
trait GenUUID[F[_]] {
  def make: F[UUID]

  /** Generates a UUID and coerces it into type A. */
  def make[A: Coercible[UUID, *]]: F[A]

  /** Tries to coerce given str into type A. */
  def read[A: Coercible[UUID, *]](str: String): F[A]
}

object GenUUID {
  def apply[F[_]](implicit ev: GenUUID[F]): GenUUID[F] = ev

  implicit def syncGenUUID[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F] {
      def make: F[UUID] =
        Sync[F].delay(UUID.randomUUID())

      def make[A: Coercible[UUID, *]]: F[A] =
        make.map(_.coerce[A])

      def read[A: Coercible[UUID, *]](str: String): F[A] =
        ApThrow[F].catchNonFatal(UUID.fromString(str).coerce[A])
    }
}
