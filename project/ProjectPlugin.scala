import sbt.Keys._
import sbt._
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = plugins.JvmPlugin

  object autoImport {

    def module(
      modName: String,
      prefix: String = "modules/"
    ): CrossProject =
      CrossProject(modName, file(s"$prefix$modName"))(JSPlatform, JVMPlatform)
        .crossType(CrossType.Pure)
        .withoutSuffixFor(JVMPlatform)
        .build()
        .jvmSettings(fork in Test := true)
        .settings(moduleName := s"${name.value}-$modName")

    lazy val noPublishSettings: Seq[Def.Setting[_]] = Seq(
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishArtifact := false)

    lazy val macroSettings: Seq[Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
        scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)))

  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(

    name := "droste",
    startYear := Option(2017),

    parallelExecution in Test := false,
    outputStrategy := Some(StdoutOutput),
    connectInput in run := true,
    cancelable in Global := true,

    crossScalaVersions :=  List("2.11.11", "2.12.6"),
    scalaVersion       := "2.12.6"
  )

}
