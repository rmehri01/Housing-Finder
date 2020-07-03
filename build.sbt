import Dependencies.Libraries

name := """housing-finder"""

scalaVersion in ThisBuild := "2.12.10"

scalacOptions += "-Ymacro-annotations"

crossScalaVersions in ThisBuild := Seq("2.12.10", "2.13.1")

lazy val commonSettings = Seq(
  scalafmtOnCompile := true,
  libraryDependencies ++= Seq(
    Libraries.cats,
    Libraries.catsEffect,
    Libraries.newtype,
    Libraries.refined,
    Libraries.squants,
    Libraries.http4sJWTAuth,
    Libraries.scalaTest % Test,
    Libraries.scalaCheck % Test,
    compilerPlugin(Libraries.kindProjector),
    compilerPlugin(Libraries.betterMonadicFor),
    compilerPlugin(Libraries.macroParadise)
  )
)

lazy val `housing-finder-root` =
  (project in file("."))
    .aggregate(`housing-finder-core`)

lazy val `housing-finder-core` = project
  .in(file("core"))
  .settings(commonSettings: _*)
