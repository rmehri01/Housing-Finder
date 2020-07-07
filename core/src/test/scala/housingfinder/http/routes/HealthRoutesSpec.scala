package housingfinder.http.routes

import cats.effect.IO
import housingfinder.algebras.HealthCheck
import housingfinder.arbitraries._
import housingfinder.domain.healthcheck.{AppStatus, PostgresStatus, RedisStatus}
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import suite.HttpTestSuite

class HealthRoutesSpec extends HttpTestSuite {

  forAll { (a: AppStatus) =>
    spec("GET healthcheck [OK]") {
      GET(uri"/healthcheck").flatMap { req =>
        val (rs, ps) = (a.redis, a.postgres)
        val routes = new HealthRoutes(dataHealthCheck(rs, ps)).routes
        assertHttp(routes, req)(
          Status.Ok,
          Map("redis" -> rs.value, "postgres" -> ps.value)
        )
      }
    }
  }

  def dataHealthCheck(rs: RedisStatus, ps: PostgresStatus): HealthCheck[IO] =
    new TestHealthCheck {
      override def status: IO[AppStatus] = IO.pure(AppStatus(rs, ps))
    }

}

protected class TestHealthCheck extends HealthCheck[IO] {
  override def status: IO[AppStatus] =
    IO.pure(AppStatus(RedisStatus(false), PostgresStatus(false)))
}
