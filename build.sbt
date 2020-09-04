lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(metaJVM, metaJS)
  .aggregate(macrosJVM, macrosJS)
  .aggregate(reftreeJVM, reftreeJS)
  .aggregate(scalacheckJVM, scalacheckJS)
  .aggregate(lawsJVM, lawsJS)
  .aggregate(testsJVM, testsJS)
  .aggregate(athemaJVM, athemaJS)
  .aggregate(readme)

lazy val publish = (project in file("."))
  .settings(noPublishSettings)
  .disablePlugins(MimaPlugin)
  .aggregate(coreJVM, coreJS)
  .aggregate(metaJVM, metaJS)
  .aggregate(macrosJVM, macrosJS)
  .aggregate(scalacheckJVM, scalacheckJS)
  .aggregate(lawsJVM, lawsJS)
  .aggregate(testsJVM, testsJS)

lazy val coverage = (project in file(".coverage"))
  .settings(noPublishSettings)
  .settings(coverageEnabled := true)
  .aggregate(coreJVM)
  .aggregate(metaJVM)
  .aggregate(macrosJVM)
  .aggregate(scalacheckJVM)
  .aggregate(lawsJVM)
  .aggregate(testsJVM)

lazy val V = new {
  val cats       = "2.2.0-RC1"
  val refined    = "0.9.15"
  val algebra    = "2.0.1"
  val atto       = "0.8.0"
  val scalacheck = "1.14.3"
  val drostePrev = "0.7.0"
}

def paradiseDep(scalaVersion: String): Seq[ModuleID] =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 =>
      Seq(
        compilerPlugin(
          "org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.patch))
    case _ => Nil
  }

lazy val meta = module("meta")
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value %%% moduleName.value % V.drostePrev),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect"  % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided")
  )

lazy val metaJVM = meta.jvm
lazy val metaJS  = meta.js

lazy val core = module("core")
  .dependsOn(meta)
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value %%% moduleName.value % V.drostePrev),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core.IncompatibleSignatureProblem
      import com.typesafe.tools.mima.core.ProblemFilters.exclude
      // See https://github.com/lightbend/mima/issues/423
      Seq(
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.Basis#Default.unapply"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GAlgebra#Gathered.unapply"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GAlgebraArrow.algebra"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GAlgebraM#Gathered.unapply"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GCoalgebra#Scattered.unapply"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GCoalgebraArrow.algebra"),
        exclude[IncompatibleSignatureProblem](
          "higherkindness.droste.GCoalgebraM#Scattered.unapply")
      )
    },
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % V.cats,
      "org.typelevel" %%% "cats-free" % V.cats)
  )

lazy val coreJVM = core.jvm
lazy val coreJS  = core.js

lazy val macros = module("macros")
  .dependsOn(core)
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value %%% moduleName.value % V.drostePrev),
    libraryDependencies ++= paradiseDep(scalaVersion.value))

lazy val macrosJVM = macros.jvm
lazy val macrosJS  = macros.js

lazy val reftree = module("reftree")
  .dependsOn(core)
  .settings(noScala213Settings)
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value                           %%% moduleName.value % V.drostePrev),
    libraryDependencies ++= Seq("io.github.stanch" %%% "reftree"        % "1.2.1"))

lazy val reftreeJVM = reftree.jvm
lazy val reftreeJS  = reftree.js

lazy val scalacheck = module("scalacheck")
  .dependsOn(core)
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value %%% moduleName.value % V.drostePrev),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % V.scalacheck)
  )

lazy val scalacheckJVM = scalacheck.jvm
lazy val scalacheckJS  = scalacheck.js

lazy val laws = module("laws")
  .dependsOn(core)
  .settings(
    mimaPreviousArtifacts := Set(
      organization.value %%% moduleName.value % V.drostePrev),
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % V.scalacheck)
  )

lazy val lawsJVM = laws.jvm
lazy val lawsJS  = laws.js

lazy val tests = module("tests")
  .dependsOn(core, scalacheck, laws, macros)
  .settings(noPublishSettings)
  .disablePlugins(MimaPlugin)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck" %%% "scalacheck"         % V.scalacheck,
    "org.typelevel"  %%% "cats-laws"          % V.cats,
    "eu.timepit"     %%% "refined"            % V.refined,
    "eu.timepit"     %%% "refined-scalacheck" % V.refined
  ) ++ paradiseDep(scalaVersion.value))

lazy val testsJVM = tests.jvm
lazy val testsJS  = tests.js

lazy val athema = module("athema", prefix = "")
  .dependsOn(core)
  .settings(noPublishSettings)
  .settings(noScala213Settings)
  .disablePlugins(MimaPlugin)
  .settings(
    libraryDependencies ++=
      Seq(
        "org.typelevel" %%% "algebra"   % V.algebra,
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
  .disablePlugins(MimaPlugin)
  .settings(
    scalacOptions in Tut ~= {
      _.filterNot(
        Set("-Ywarn-unused-import", "-Yno-predef", "-Ywarn-unused:imports"))
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
  .disablePlugins(MimaPlugin)
  .settings(
    scalacOptions in Tut ~= (_ filterNot Set("-Ywarn-unused-import", "-Xlint").contains)
  )
