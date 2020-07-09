package tokens

import cats.effect._
import dev.profunktor.auth.jwt._
import io.circe.parser.{decode => jsonDecode}
import io.circe.{Decoder, Json}
import io.estatico.newtype.macros._
import pdi.jwt._
import pdi.jwt.algorithms.JwtHmacAlgorithm

// Taken from https://github.com/gvolpe/pfps-shopping-cart/blob/master/modules/core/src/main/scala/tokens/generator.scala
// To make it runnable, change `class` for `object`.
class RunnableTokenGenerator extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    TokenGenerator.run
}

object TokenGenerator {
  import data._

  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  /* ---- Encoding stuff ---- */

  // A Claim can be any valid JSON
  val claim: JwtClaim = JwtClaim(
    """
      {"claim": "example-claim"}
    """
  )

  // Any valid string
  val secretKey: JwtSecretKey = JwtSecretKey("any-secret")

  val algo: JwtHmacAlgorithm = JwtAlgorithm.HS256

  def mkToken(c: JwtClaim, s: JwtSecretKey): IO[JwtToken] =
    jwtEncode[IO](c, s, algo)

  /* ---- Decoding stuff ---- */

  val jwtAuth: JwtSymmetricAuth = JwtAuth.hmac(secretKey.value, algo)

  def decodeToken(token: JwtToken, a: JwtSymmetricAuth): IO[Claim] =
    jwtDecode[IO](token, a).flatMap { c =>
      IO.fromEither(jsonDecode[Claim](c.content))
    }

  def run: IO[ExitCode] =
    for {
      t <- mkToken(claim, secretKey)
      _ <- putStrLn(t)
      c <- decodeToken(t, jwtAuth)
      _ <- putStrLn(c)
    } yield ExitCode.Success

}

object data {
  @newtype case class Claim(value: Json)

  object Claim {
    implicit val jsonDecoder: Decoder[Claim] =
      Decoder.forProduct1("claim")(Claim.apply)
  }
}
