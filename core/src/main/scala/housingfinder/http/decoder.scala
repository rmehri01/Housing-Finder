package housingfinder.http

import cats.implicits._
import housingfinder.effects._
import io.circe.Decoder
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

object decoder {

  implicit class RefinedRequestDecoder[F[_]: JsonDecoder: MonadThrow](
      req: Request[F]
  ) extends Http4sDsl[F] {

    /** Decodes a request into a type A and then applies f to it in order to create a response.
      *
      * Deals with validation errors from Refined and returns 400 Bad Request rather than 422 Unprocessable Entity.
      */
    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      req.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.contains("predicate failed") =>
              BadRequest(c.getMessage)
            case _ => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }

  }

}
