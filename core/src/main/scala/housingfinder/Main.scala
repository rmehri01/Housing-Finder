package housingfinder

import cats.effect._
import housingfinder.modules._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") *>
        AppResources.make[IO](cfg).use { res =>
          for {
            security <- Security.make[IO](cfg, res.psql, res.redis)
            clients <- HttpClients.make(res.client)
            algebras <- Algebras.make[IO](res.redis, res.psql, clients)
            programs <- Programs.make(algebras)
            api <- HttpApi.make[IO](algebras, programs, security)
            _ <-
              BlazeServerBuilder[IO](ExecutionContext.global)
                .bindHttp(
                  cfg.httpServerConfig.port.value,
                  cfg.httpServerConfig.host.value
                )
                .withHttpApp(api.httpApp)
                .withResponseHeaderTimeout(
                  cfg.httpServerConfig.responseHeaderTimeout
                )
                .serve
                .compile
                .drain
          } yield ExitCode.Success
        }
    }

}
