resolvers += Resolver.url(
  "typesafe sbt-plugins",
  url("https://dl.bintray.com/typesafe/sbt-plugins")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.47deg"           % "sbt-microsites"                % "1.2.0")
addSbtPlugin("com.alejandrohdezma" % "sbt-github"                    % "0.9.0")
addSbtPlugin("com.alejandrohdezma" % "sbt-github-mdoc"               % "0.9.0")
addSbtPlugin("com.geirsson"        % "sbt-ci-release"                % "1.5.5")
addSbtPlugin("com.typesafe"        % "sbt-mima-plugin"               % "0.7.0")
addSbtPlugin("org.portable-scala"  % "sbt-scala-native-crossproject" % "1.0.0")
addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"                   % "1.1.1")
addSbtPlugin("org.scala-native"    % "sbt-scala-native"              % "0.3.9")
addSbtPlugin("org.scalameta"       % "sbt-mdoc"                      % "2.2.13")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"                  % "2.4.2")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"                 % "1.6.1")
