lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(metaJVM, metaJS)
  .aggregate(lawsJVM, lawsJS)
  .aggregate(testsJVM, testsJS)
  .aggregate(athemaJVM, athemaJS)
  .aggregate(readme)

lazy val meta = module("meta")
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"))

lazy val metaJVM = meta.jvm
lazy val metaJS  = meta.js

lazy val core = module("core")
  .dependsOn(meta)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core"   % "1.1.0",
    "org.typelevel" %%% "cats-free"   % "1.1.0"))

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val laws = module("laws")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % "1.14.0"))

lazy val lawsJVM = laws.jvm
lazy val lawsJS  = laws.js

lazy val tests = module("tests")
  .dependsOn(core)
  .dependsOn(laws)
  .dependsOn(athema)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck"         % "1.14.0",
    "org.typelevel"  %%% "algebra"            % "1.0.0",
    "org.typelevel"  %%% "cats-laws"          % "1.1.0",
    "eu.timepit"     %%% "refined"            % "0.9.2",
    "eu.timepit"     %%% "refined-scalacheck" % "0.9.2"))

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
      "org.scalacheck" %%% "scalacheck" % "1.14.0"
    ).map(_ % "test"))


lazy val athemaJVM = athema.jvm
lazy val athemaJS  = athema.js

lazy val readme = (project in file("modules/readme"))
  .enablePlugins(TutPlugin)
  .dependsOn(coreJVM)
  .dependsOn(athemaJVM)
  .settings(noPublishSettings)
  .settings(
    scalacOptions in Tut ~= {
      _.filterNot(Set(
        "-Ywarn-unused-import",
        "-Yno-predef",
        "-Ywarn-unused:imports"))
    },
    tutTargetDirectory := (baseDirectory in LocalRootProject).value
  )
