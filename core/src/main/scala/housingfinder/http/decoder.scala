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

    // deals with validation errors from Refined and returns 400 rather than 422
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
