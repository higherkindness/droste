
lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(metaJVM, metaJS)
  .aggregate(macrosJVM, macrosJS)
  .aggregate(reftreeJVM, reftreeJS)
  .aggregate(lawsJVM, lawsJS)
  .aggregate(testsJVM, testsJS)
  .aggregate(athemaJVM, athemaJS)
  .aggregate(readme)

lazy val V = new {
  val cats       = "1.2.0"
  val refined    = "0.9.0"
  val algebra    = "1.0.0"
  val atto       = "0.6.2"
  val scalacheck = "1.13.5"
}

lazy val meta = module("meta")
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
    "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"))

lazy val metaJVM = meta.jvm
lazy val metaJS  = meta.js

lazy val core = module("core")
  .dependsOn(meta)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core"   % V.cats,
    "org.typelevel" %%% "cats-free"   % V.cats))

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val macros = module("macros")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
  ))

lazy val macrosJVM = macros.jvm
lazy val macrosJS  = macros.js

lazy val reftree = module("reftree")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "io.github.stanch" %%% "reftree" % "1.2.1"))

lazy val reftreeJVM = reftree.jvm
lazy val reftreeJS  = reftree.js

lazy val laws = module("laws")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck" % V.scalacheck))

lazy val lawsJVM = laws.jvm
lazy val lawsJS  = laws.js

lazy val tests = module("tests")
  .dependsOn(core, laws, athema, macros)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck"         % V.scalacheck,
    "org.typelevel"  %%% "algebra"            % V.algebra,
    "org.typelevel"  %%% "cats-laws"          % V.cats,
    "eu.timepit"     %%% "refined"            % V.refined,
    "eu.timepit"     %%% "refined-scalacheck" % V.refined,
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)))

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js

lazy val athema = module("athema", prefix = "")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++=
    Seq(
      "org.typelevel" %%% "algebra" % V.algebra,
      "org.tpolecat"  %%% "atto-core" % V.atto
    ) ++
    Seq(
      "org.scalacheck" %%% "scalacheck" % V.scalacheck
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

///////////////
//// DOCS ////
///////////////

lazy val docs = (project in file("docs"))
  .dependsOn(coreJVM)
  .dependsOn(athemaJVM)
  .settings(moduleName := "droste-docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .enablePlugins(MicrositesPlugin)
  .disablePlugins(ProjectPlugin)
  .settings(
    scalacOptions in Tut ~= (_ filterNot Set("-Ywarn-unused-import", "-Xlint").contains)
  )