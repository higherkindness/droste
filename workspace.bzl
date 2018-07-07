def droste_bind_dependencies(
    cats_core = "//3rdparty/jvm/org/typelevel:cats_core",
    cats_free = "//3rdparty/jvm/org/typelevel:cats_free",
    kind_projector_jar = "//external:jar/io_higherkindness_droste_org/spire_math/kind_projector_2_11",
):

    native.bind(
        name = "io_higherkindness_droste_typelevel_cats_core",
        actual = cats_core,
    )

    native.bind(
        name = "io_higherkindness_droste_typelevel_cats_free",
        actual = cats_free,
    )

    native.bind(
        name = "io_higherkindness_droste_spire_math_kind_projector_jar",
        actual = kind_projector_jar
    )
