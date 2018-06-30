lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(catsJVM, catsJS)
  .aggregate(testsJVM, testsJS)

lazy val meta = module("meta")
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"))

lazy val metaJVM = meta.jvm
lazy val metaJS  = meta.js

lazy val core = module("core")
  .dependsOn(meta)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core"   % "1.0.1",
    "org.typelevel" %%% "cats-free"   % "1.0.1"))

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val cats = module("cats")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core"   % "1.0.1",
    "org.typelevel" %%% "cats-free"   % "1.0.1"))

lazy val catsJVM = cats.jvm
lazy val catsJS  = cats.js

lazy val tests = module("tests")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.4"))

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js
