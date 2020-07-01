import sbt._

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val catsEffect = "2.1.3"

    // Test
    val scalaTest = "3.2.0"
    val scalaCheck = "1.14.3"

    // Compiler
    val kindProjector = "0.10.3"
    val betterMonadicFor = "0.3.0"
  }

  object Libraries {
    lazy val cats = "org.typelevel" %% "cats-core" % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect

    // Test
    lazy val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest
    lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck

    // Compiler
    lazy val kindProjector =
      "org.typelevel" %% "kind-projector" % Versions.kindProjector
    lazy val betterMonadicFor =
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
  }

}
