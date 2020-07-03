import sbt._

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val catsEffect = "2.1.3"
    val newtype = "0.4.4"
    val refined = "0.9.14"
    val squants = "1.6.0"
    val http4s = "0.21.6"
    val http4sJwtAuth = "0.0.5"
    val circe = "0.13.0"

    // Test
    val scalaTest = "3.2.0"
    val scalaCheck = "1.14.3"

    // Compiler
    val kindProjector = "0.10.3"
    val betterMonadicFor = "0.3.0"
    val macroParadise = "2.1.1"
  }

  object Libraries {
    def circe(artifact: String): ModuleID =
      "io.circe" %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID =
      "org.http4s" %% artifact % Versions.http4s

    lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val newtype = "io.estatico" %% "newtype" % Versions.newtype
    lazy val refined = "eu.timepit" %% "refined" % Versions.refined
    lazy val squants = "org.typelevel" %% "squants" % Versions.squants

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

    // Test
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck

    // Compiler
    lazy val kindProjector =
      "org.typelevel" %% "kind-projector" % Versions.kindProjector
    lazy val betterMonadicFor =
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    lazy val macroParadise =
      "org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full
  }

}
