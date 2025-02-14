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
    Libraries.refinedCore,
    Libraries.refinedCats,
    Libraries.squants,
    Libraries.log4cats,
    Libraries.logback % Runtime,
    Libraries.http4sDsl,
    Libraries.http4sServer,
    Libraries.http4sClient,
    Libraries.http4sCirce,
    Libraries.http4sJwtAuth,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.circeRefined,
    Libraries.skunkCore,
    Libraries.skunkCirce,
    Libraries.cirisCore,
    Libraries.cirisEnum,
    Libraries.cirisRefined,
    Libraries.redis4catsEffects,
    Libraries.redis4catsLog4cats,
    Libraries.scalaScraper,
    Libraries.scalaTest % Test,
    Libraries.scalaCheck % Test,
    Libraries.scalaTestPlus % Test,
    Libraries.circeTesting % Test,
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
  .enablePlugins(DockerPlugin, AshScriptPlugin)
  .settings(
    packageName in Docker := "housing-finder",
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true
  )
  .settings(commonSettings: _*)
