ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject"      % "1.2.0")
addSbtPlugin("org.portable-scala"  % "sbt-scala-native-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"                   % "1.10.1")
addSbtPlugin("org.scala-native"    % "sbt-scala-native"              % "0.4.5")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"                 % "2.0.2")
addSbtPlugin("com.github.sbt"      % "sbt-ci-release"                % "1.5.10")
addSbtPlugin("com.47deg"           % "sbt-microsites"                % "1.3.4")
addSbtPlugin("org.scalameta"       % "sbt-mdoc"                      % "2.3.2")
addSbtPlugin("com.alejandrohdezma" % "sbt-github"                    % "0.11.2")
addSbtPlugin("com.alejandrohdezma" % "sbt-github-mdoc"               % "0.11.2")
addSbtPlugin("com.typesafe"        % "sbt-mima-plugin"               % "1.1.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.1")
