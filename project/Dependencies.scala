import sbt._

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val catsEffect = "2.1.3"
    val newtype = "0.4.4"
    val refined = "0.9.14"
    val squants = "1.6.0"

    // Test
    val scalaTest = "3.2.0"
    val scalaCheck = "1.14.3"

    // Compiler
    val kindProjector = "0.10.3"
    val betterMonadicFor = "0.3.0"
    val macroParadise = "2.1.1"
  }

  object Libraries {
    lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val newtype = "io.estatico" %% "newtype" % Versions.newtype
    lazy val refined = "eu.timepit" %% "refined" % Versions.refined
    lazy val squants = "org.typelevel" %% "squants" % Versions.squants

    // Test
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck

    // Compiler
    lazy val kindProjector =
      "org.typelevel" %% "kind-projector" % Versions.kindProjector
    lazy val betterMonadicFor =
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    val macroParadise =
      "org.scalamacros" % "paradise" % Versions.macroParadise cross CrossVersion.full
  }

}
