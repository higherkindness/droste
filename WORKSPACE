workspace(name = "io_higherkindness_droste")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_scala_version = "f3113fb6e9e35cb8f441d2305542026d98afc0a2"  # update this as needed

http_archive(
    name = "io_bazel_rules_scala",
    strip_prefix = "rules_scala-%s" % rules_scala_version,
    type = "zip",
    url = "https://github.com/bazelbuild/rules_scala/archive/%s.zip" % rules_scala_version,
)

load("@io_bazel_rules_scala//scala:scala.bzl", "scala_repositories")

scala_repositories()

load("@io_bazel_rules_scala//scala:toolchains.bzl", "scala_register_toolchains")

scala_register_toolchains()

load("//3rdparty:workspace.bzl", "maven_dependencies")

maven_dependencies()

load(":workspace.bzl", "droste_bind_dependencies")

droste_bind_dependencies()
