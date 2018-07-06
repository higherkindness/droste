# Do not edit. bazel-deps autogenerates this file from bazel_deps.yaml.

def declare_maven(hash):
    native.maven_jar(
        name = hash["name"],
        artifact = hash["artifact"],
        sha1 = hash["sha1"],
        repository = hash["repository"]
    )
    native.bind(
        name = hash["bind"],
        actual = hash["actual"]
    )

def list_dependencies():
    return [
    {"artifact": "org.scala-lang.modules:scala-parser-combinators_2.11:1.0.4", "lang": "java", "sha1": "7369d653bcfa95d321994660477a4d7e81d7f490", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_scala_lang_modules_scala_parser_combinators_2_11", "actual": "@io_higherkindness_droste_org_scala_lang_modules_scala_parser_combinators_2_11//jar", "bind": "jar/io_higherkindness_droste_org/scala_lang/modules/scala_parser_combinators_2_11"},
    {"artifact": "org.scala-lang.modules:scala-xml_2.11:1.0.5", "lang": "java", "sha1": "77ac9be4033768cf03cc04fbd1fc5e5711de2459", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_scala_lang_modules_scala_xml_2_11", "actual": "@io_higherkindness_droste_org_scala_lang_modules_scala_xml_2_11//jar", "bind": "jar/io_higherkindness_droste_org/scala_lang/modules/scala_xml_2_11"},
    {"artifact": "org.scala-lang:scala-compiler:2.11.12", "lang": "java", "sha1": "a1b5e58fd80cb1edc1413e904a346bfdb3a88333", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_scala_lang_scala_compiler", "actual": "@io_higherkindness_droste_org_scala_lang_scala_compiler//jar", "bind": "jar/io_higherkindness_droste_org/scala_lang/scala_compiler"},
# duplicates in org.scala-lang:scala-library promoted to 2.11.12
# - org.scala-lang.modules:scala-parser-combinators_2.11:1.0.4 wanted version 2.11.6
# - org.scala-lang.modules:scala-xml_2.11:1.0.5 wanted version 2.11.7
# - org.scala-lang:scala-compiler:2.11.12 wanted version 2.11.12
# - org.scala-lang:scala-reflect:2.11.12 wanted version 2.11.12
# - org.spire-math:kind-projector_2.11:0.9.7 wanted version 2.11.12
# - org.typelevel:cats-core_2.11:1.1.0 wanted version 2.11.12
# - org.typelevel:cats-free_2.11:1.1.0 wanted version 2.11.12
# - org.typelevel:cats-kernel_2.11:1.1.0 wanted version 2.11.12
# - org.typelevel:cats-macros_2.11:1.1.0 wanted version 2.11.12
# - org.typelevel:machinist_2.11:0.6.2 wanted version 2.11.8
    {"artifact": "org.scala-lang:scala-library:2.11.12", "lang": "java", "sha1": "bf5534e6fec3d665bd6419c952a929a8bdd4b591", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_scala_lang_scala_library", "actual": "@io_higherkindness_droste_org_scala_lang_scala_library//jar", "bind": "jar/io_higherkindness_droste_org/scala_lang/scala_library"},
# duplicates in org.scala-lang:scala-reflect promoted to 2.11.12
# - org.scala-lang:scala-compiler:2.11.12 wanted version 2.11.12
# - org.typelevel:machinist_2.11:0.6.2 wanted version 2.11.8
    {"artifact": "org.scala-lang:scala-reflect:2.11.12", "lang": "java", "sha1": "2bb23c13c527566d9828107ca4108be2a2c06f01", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_scala_lang_scala_reflect", "actual": "@io_higherkindness_droste_org_scala_lang_scala_reflect//jar", "bind": "jar/io_higherkindness_droste_org/scala_lang/scala_reflect"},
    {"artifact": "org.spire-math:kind-projector_2.11:0.9.7", "lang": "scala", "sha1": "9dd7e57630f5a3765e7a65c08a02f83cecc59ff7", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_spire_math_kind_projector_2_11", "actual": "@io_higherkindness_droste_org_spire_math_kind_projector_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/spire_math/kind_projector_2_11"},
    {"artifact": "org.typelevel:cats-core_2.11:1.1.0", "lang": "scala", "sha1": "854ab2123eccb2edc7bf00a484cf7826626ce71d", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_typelevel_cats_core_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_core_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_core_2_11"},
    {"artifact": "org.typelevel:cats-free_2.11:1.1.0", "lang": "scala", "sha1": "a43effe7b5c1d9a1d1dd105865e4afe987e6e402", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_typelevel_cats_free_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_free_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_free_2_11"},
    {"artifact": "org.typelevel:cats-kernel_2.11:1.1.0", "lang": "scala", "sha1": "45051dfb4703364929d603ac06afcda4a421d8da", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_typelevel_cats_kernel_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_kernel_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_kernel_2_11"},
    {"artifact": "org.typelevel:cats-macros_2.11:1.1.0", "lang": "java", "sha1": "59a4fa3d642046b3997cfcc2db43f4df736545f2", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_typelevel_cats_macros_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_macros_2_11//jar", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_macros_2_11"},
    {"artifact": "org.typelevel:machinist_2.11:0.6.2", "lang": "java", "sha1": "029c6a46d66b6616f8795a70753e6753975f42fc", "repository": "http://central.maven.org/maven2/", "name": "io_higherkindness_droste_org_typelevel_machinist_2_11", "actual": "@io_higherkindness_droste_org_typelevel_machinist_2_11//jar", "bind": "jar/io_higherkindness_droste_org/typelevel/machinist_2_11"},
    ]

def maven_dependencies(callback = declare_maven):
    for hash in list_dependencies():
        callback(hash)
