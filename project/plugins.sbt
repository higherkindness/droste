ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject"      % "1.3.2")
addSbtPlugin("org.portable-scala"  % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"                   % "1.17.0")
addSbtPlugin("org.scala-native"    % "sbt-scala-native"              % "0.5.5")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"                 % "2.1.0")
addSbtPlugin("com.github.sbt"      % "sbt-ci-release"                % "1.6.1")
addSbtPlugin("com.47deg"           % "sbt-microsites"                % "1.4.4")
addSbtPlugin("org.scalameta"       % "sbt-mdoc"                      % "2.5.4")
addSbtPlugin("com.alejandrohdezma" % "sbt-github"                    % "0.12.0")
addSbtPlugin("com.alejandrohdezma" % "sbt-github-mdoc"               % "0.12.0")
addSbtPlugin("com.typesafe"        % "sbt-mima-plugin"               % "1.1.4")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.1")
