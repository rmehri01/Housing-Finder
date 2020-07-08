package integration

import cats.effect.{IO, Resource}
import dev.profunktor.redis4cats._
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.log4cats._
import housingfinder.logger.NoOp
import suite.ResourceSuite

class RedisTest extends ResourceSuite[RedisCommands[IO, String, String]] {

  override def resources: Resource[IO, RedisCommands[IO, String, String]] =
    for {
      uri <- Resource.liftF(RedisURI.make[IO]("redis://localhost"))
      client <- RedisClient[IO](uri)
      cmd <- Redis[IO].fromClient[String, String](client, RedisCodec.Utf8)
    } yield cmd

}
