package tokens

import cats.effect.{ExitCode, IO}
import cats.implicits._
import dev.profunktor.auth.jwt.{JwtAuth, JwtSecretKey}
import io.circe.Json
import io.circe.testing.ArbitraryInstances
import pdi.jwt.JwtClaim
import suite.PureTestSuite
import tokens.TokenGenerator._
import utilities.arbitraries._

class GeneratorSpec extends PureTestSuite with ArbitraryInstances {
  forAll { (j: Json, s: JwtSecretKey) =>
    spec("Token is made and decoded successfully") {
      val c = JwtClaim(Json.obj(("claim", j)).toString)
      for {
        // encoding and decoding the claim should result in the same Json
        t <- mkToken(c, s)
        a = JwtAuth.hmac(s.value, algo)
        c <- decodeToken(t, a)
      } yield assert(c.value == j)
    }
  }

  spec("Main runs properly with default settings") {
    assert(
      TokenGenerator.run.unsafeRunSync == ExitCode.Success
    ).pure[IO]
  }
}
