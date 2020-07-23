package housingfinder

import cats.Parallel
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats._
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import housingfinder.config.data._
import io.chrisdavenport.log4cats.Logger
import natchez.Trace.Implicits.noop
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import skunk._

import scala.concurrent.ExecutionContext

final case class AppResources[F[_]] private (
    client: Client[F],
    psql: Resource[F, Session[F]],
    redis: RedisCommands[F, String, String]
)

object AppResources {

  /** Smart constructor that uses an [[AppConfig]] to create the resources for the application. */
  def make[F[_]: ConcurrentEffect: ContextShift: Logger: Timer: Parallel](
      cfg: AppConfig
  ): Resource[F, AppResources[F]] = {

    def mkPostgreSqlResource(c: PostgreSQLConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host.value,
          port = c.port.value,
          user = c.user.value,
          database = c.database.value,
          max = c.max.value
        )

    def mkRedisResource(
        c: RedisConfig
    ): Resource[F, RedisCommands[F, String, String]] =
      for {
        uri <- Resource.liftF(RedisURI.make[F](c.uri.value.value))
        client <- RedisClient[F](uri)
        cmd <- Redis[F].fromClient[String, String](client, RedisCodec.Utf8)
      } yield cmd

    def mkHttpClient(c: HttpClientConfig): Resource[F, Client[F]] =
      BlazeClientBuilder[F](ExecutionContext.global)
        .withConnectTimeout(c.connectTimeout)
        .withRequestTimeout(c.requestTimeout)
        .resource

    (
      mkHttpClient(cfg.httpClientConfig),
      mkPostgreSqlResource(cfg.postgreSQL),
      mkRedisResource(cfg.redis)
    ).parMapN(AppResources.apply[F])

  }

}
