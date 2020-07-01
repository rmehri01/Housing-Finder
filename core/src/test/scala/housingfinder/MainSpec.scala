package housingfinder

import cats.effect._
import org.scalatest._
import flatspec._
import matchers._

class MainSpec extends AnyFlatSpec with should.Matchers {
  "Main" should "run a println" in {
    Main.run(List.empty[String]).unsafeRunSync shouldBe ExitCode.Success
  }
}
