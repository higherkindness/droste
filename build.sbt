lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(testsJVM, testsJS)
  .aggregate(athemaJVM, athemaJS)

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

lazy val tests = module("tests")
  .dependsOn(core)
  .dependsOn(athema)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.13.4",
    "org.typelevel" %%% "algebra" % "1.0.0"))

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js

lazy val athema = module("athema", prefix = "")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++=
    Seq(
      "org.typelevel" %%% "algebra" % "1.0.0",
      "org.tpolecat" %%% "atto-core" % "0.6.2"
    ) ++
    Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.4"
    ).map(_ % "test"))


lazy val athemaJVM = athema.jvm
lazy val athemaJS  = athema.js
