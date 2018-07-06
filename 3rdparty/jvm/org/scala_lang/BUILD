load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "scala_compiler",
    exports = [
        "//3rdparty/jvm/org/scala_lang/modules:scala_parser_combinators_2_11",
        "//3rdparty/jvm/org/scala_lang/modules:scala_xml_2_11",
        "//external:jar/io_higherkindness_droste_org/scala_lang/scala_compiler",
        ":scala_library",
        ":scala_reflect"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)



java_library(
    name = "scala_library",
    exports = [
        "//external:jar/io_higherkindness_droste_org/scala_lang/scala_library"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)



java_library(
    name = "scala_reflect",
    exports = [
        "//external:jar/io_higherkindness_droste_org/scala_lang/scala_reflect",
        ":scala_library"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


