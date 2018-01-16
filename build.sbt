lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(testsJVM, testsJS)

lazy val core = module("core")
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core"   % "1.0.1",
    "org.typelevel" %%% "cats-free"   % "1.0.1"))

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val tests = module("tests")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.4"))

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js
