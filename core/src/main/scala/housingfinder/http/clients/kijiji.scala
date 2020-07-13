package housingfinder.http.clients

trait KijijiClient[F[_]] {
  // TODO: newtypes
  def getBasePage(n: Int): F[String]
  def getHtml(relativeUrl: String): F[String]
}

