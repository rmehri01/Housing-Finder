import Dependencies.Libraries

name := """housing-finder"""

scalaVersion in ThisBuild := "2.12.10"

crossScalaVersions in ThisBuild := Seq("2.12.10", "2.13.1")

lazy val commonSettings = Seq(
  scalafmtOnCompile := true,
  libraryDependencies ++= Seq(
    Libraries.cats,
    Libraries.catsEffect,
    Libraries.scalaTest % Test,
    Libraries.scalaCheck % Test,
    compilerPlugin(Libraries.kindProjector),
    compilerPlugin(Libraries.betterMonadicFor)
  )
)

lazy val `housing-finder-root` =
  (project in file("."))
    .aggregate(`housing-finder-core`)

lazy val `housing-finder-core` = project
  .in(file("core"))
  .settings(commonSettings: _*)
