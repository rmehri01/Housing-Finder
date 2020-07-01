package housingfinder

import cats.effect._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    IO(println("Hello")).as(ExitCode.Success)
}
