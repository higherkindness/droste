load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
scala_import(
    name = "cats_core",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":cats_kernel",
        ":cats_macros",
        ":machinist"
    ],
    jars = [
        "//external:jar/io_higherkindness_droste_org/typelevel/cats_core_2_11"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "cats_free",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":cats_core",
        ":cats_macros",
        ":machinist"
    ],
    jars = [
        "//external:jar/io_higherkindness_droste_org/typelevel/cats_free_2_11"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "cats_kernel",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library"
    ],
    jars = [
        "//external:jar/io_higherkindness_droste_org/typelevel/cats_kernel_2_11"
    ],
    visibility = [
        "//visibility:public"
    ]
)



scala_import(
    name = "cats_macros",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        ":machinist"
    ],
    jars = [
        "//external:jar/io_higherkindness_droste_org/typelevel/cats_macros_2_11"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)



scala_import(
    name = "machinist",
    exports = [
        "//3rdparty/jvm/org/scala_lang:scala_library",
        "//3rdparty/jvm/org/scala_lang:scala_reflect"
    ],
    jars = [
        "//external:jar/io_higherkindness_droste_org/typelevel/machinist_2_11"
    ],
    visibility = [
        "//3rdparty/jvm:__subpackages__"
    ]
)


