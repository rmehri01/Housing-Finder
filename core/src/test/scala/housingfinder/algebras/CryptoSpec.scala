package housingfinder.algebras

import cats.effect.IO
import housingfinder.config.data.PasswordSalt
import housingfinder.domain.auth.Password
import suite.PureTestSuite
import utilities.arbitraries._

class CryptoSpec extends PureTestSuite {
  forAll { (s: PasswordSalt, p: Password) =>
    spec("Password is successfully encrypted and decrypted") {
      for {
        c <- LiveCrypto.make[IO](s)
        encrypted = c.encrypt(p)
        decrypted = c.decrypt(encrypted)
      } yield assert(p == decrypted)
    }
  }
}
