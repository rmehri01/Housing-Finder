package suite

import java.util.UUID

import cats.effect._
import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.ExecutionContext

/** Test suite built on top of existing popular ones to hide side-effects by testing within [[IO]].
  *
  * Built on top of ScalaTest with support for asynchronous tests, property-based testing with ScalaCheck,
  * and Cats equality.
  */
trait PureTestSuite
    extends AsyncFunSuite
    with ScalaCheckDrivenPropertyChecks
    with CatsEquality {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private def mkUnique(name: String): String =
    s"$name - ${UUID.randomUUID}"

  def spec(
      testName: String
  )(f: => IO[Assertion])(implicit pos: Position): Unit =
    test(mkUnique(testName))(IO.suspend(f).unsafeToFuture())

}
