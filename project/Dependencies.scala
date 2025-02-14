import sbt._

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val catsEffect = "2.1.3"
    val newtype = "0.4.4"
    val refined = "0.9.14"
    val squants = "1.6.0"
    val log4cats = "1.1.1"
    val http4s = "0.21.6"
    val http4sJwtAuth = "0.0.5"
    val circe = "0.13.0"
    val skunk = "0.0.13"
    val ciris = "1.1.1"
    val redis4cats = "0.10.1"
    val scalaScraper = "2.2.0"

    // Test
    val scalaTest = "3.2.0"
    val scalaCheck = "1.14.3"
    val scalaTestPlus = "3.2.0.0"

    // Compiler
    val kindProjector = "0.10.3"
    val betterMonadicFor = "0.3.0"
    val macroParadise = "2.1.1"

    // Runtime
    val logback = "1.2.3"
  }

  object Libraries {
    def circe(artifact: String): ModuleID =
      "io.circe" %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% artifact % Versions.http4s
    def skunk(artifact: String): ModuleID =
      "org.tpolecat" %% artifact % Versions.skunk
    def refined(artifact: String): ModuleID =
      "eu.timepit" %% artifact % Versions.refined
    def ciris(artifact: String): ModuleID =
      "is.cir" %% artifact % Versions.ciris
    def redis4cats(artifact: String): ModuleID =
      "dev.profunktor" %% artifact % Versions.redis4cats

    lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val newtype = "io.estatico" %% "newtype" % Versions.newtype
    lazy val squants = "org.typelevel" %% "squants" % Versions.squants
    lazy val log4cats = "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4cats

    lazy val refinedCats = refined("refined-cats")
    lazy val refinedCore = refined("refined")

    lazy val http4sDsl = http4s("http4s-dsl")
    lazy val http4sServer = http4s("http4s-blaze-server")
    lazy val http4sClient = http4s("http4s-blaze-client")
    lazy val http4sCirce = http4s("http4s-circe")

    lazy val http4sJwtAuth =
      "dev.profunktor" %% "http4s-jwt-auth" % Versions.http4sJwtAuth

    lazy val circeCore = circe("circe-core")
    lazy val circeGeneric = circe("circe-generic")
    lazy val circeParser = circe("circe-parser")
    lazy val circeRefined = circe("circe-refined")

    lazy val skunkCore = skunk("skunk-core")
    lazy val skunkCirce = skunk("skunk-circe")

    lazy val cirisCore = ciris("ciris")
    lazy val cirisEnum = ciris("ciris-enumeratum")
    lazy val cirisRefined = ciris("ciris-refined")

    lazy val redis4catsEffects = redis4cats("redis4cats-effects")
    lazy val redis4catsLog4cats = redis4cats("redis4cats-log4cats")

    lazy val scalaScraper = "net.ruippeixotog" %% "scala-scraper" % Versions.scalaScraper

    // Test
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
    lazy val scalaTestPlus =
      "org.scalatestplus" %% "scalacheck-1-14" % Versions.scalaTestPlus
    lazy val circeTesting = circe("circe-testing")

    // Compiler
    lazy val kindProjector =
      "org.typelevel" %% "kind-projector" % Versions.kindProjector
    lazy val betterMonadicFor =
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    lazy val macroParadise =
      "org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
  }

}
