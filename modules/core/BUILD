load("@io_bazel_rules_scala//scala:scala.bzl", "scala_library")

scala_library(
    name = "core",
    srcs = glob(["**/*.scala"]),
    plugins = [
        "//external:io_higherkindness_droste_spire_math_kind_projector_jar",
    ],
    scalacopts = [
        "-encoding", "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:experimental.macros",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-Ypartial-unification",
    ],    
    deps = [
        "//external:io_higherkindness_droste_typelevel_cats_core",
        "//external:io_higherkindness_droste_typelevel_cats_free",
        "//modules/meta",
    ],    
    visibility = [
        "//visibility:public"
    ],
)
