workspace(name = "io_higherkindness_droste")

rules_scala_version="63eab9f4d80612e918ba954211f377cc83d27a07" # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip"%rules_scala_version,
    type = "zip",
    strip_prefix= "rules_scala-%s" % rules_scala_version
)

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")
scala_repositories()

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")
scala_register_toolchains()

load("//3rdparty:workspace.bzl", "maven_dependencies")
maven_dependencies()

load(":workspace.bzl", "droste_bind_dependencies")
droste_bind_dependencies()
