import com.typesafe.sbt.site.jekyll.JekyllPlugin.autoImport._
import com.typesafe.sbt.site.SitePlugin.autoImport._
import microsites.MicrositeKeys._
import sbt.Keys._
import sbt._
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossPlugin.autoImport._
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import scalanativecrossproject.ScalaNativeCrossPlugin.autoImport._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins      = plugins.JvmPlugin

  object autoImport {

    def module(
        modName: String,
        prefix: String = "modules/"
    ): CrossProject =
      CrossProject(modName, file(s"$prefix$modName"))(
        JSPlatform,
        JVMPlatform /*, NativePlatform // soon */ )
        .crossType(CrossType.Pure)
        .withoutSuffixFor(JVMPlatform)
        .build()
        .jvmSettings(fork in Test := true)
        .settings(moduleName := s"droste-$modName")

    def jvmModule(
        modName: String,
        prefix: String = "modules/"
    ): Project =
      Project(modName, file(s"$prefix$modName"))
        .settings(
          fork in Test := true,
          moduleName := s"droste-$modName"
        )

    lazy val macroSettings: Seq[Setting[_]] = Seq(
      libraryDependencies ++= Seq(
        scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
        scalaOrganization.value % "scala-reflect"  % scalaVersion.value % Provided,
        compilerPlugin(
          "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.patch)
      ))

    lazy val noPublishSettings: Seq[Def.Setting[_]] = Seq(
      publish := ((): Unit),
      publishLocal := ((): Unit),
      publishArtifact := false)

    lazy val micrositeSettings = Seq(
      micrositeName := "Droste",
      micrositeDescription := "A recursion library for Scala",
      micrositeDocumentationUrl := "/docs/",
      micrositeAuthor := "Andy Scott",
      micrositeGithubOwner := "andyscott",
      micrositeGithubRepo := "droste",
      micrositeGitterChannelUrl := "droste-recursion/Lobby",
      micrositeExternalLayoutsDirectory := (resourceDirectory in Compile).value / "microsite" / "layouts",
      micrositeExternalIncludesDirectory := (resourceDirectory in Compile).value / "microsite" / "includes",
      includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" | "*.svg" | "*.json" | "CNAME",
      includeFilter in Jekyll := (includeFilter in makeSite).value,
      micrositePushSiteWith := GitHub4s
    )

    lazy val noScala213Settings: Seq[Def.Setting[_]] = Seq(
      crossScalaVersions ~= (_.filterNot(_.startsWith("2.13")))
    )

  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      organization := "io.higherkindness",
      name := "droste",
      startYear := Option(2018),
      parallelExecution in Test := false,
      outputStrategy := Some(StdoutOutput),
      connectInput in run := true,
      cancelable in Global := true,
      crossScalaVersions := List("2.12.10", "2.13.1"),
      scalaVersion := "2.12.10"
    )

}
