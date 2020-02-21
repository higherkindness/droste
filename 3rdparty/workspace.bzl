# Do not edit. bazel-deps autogenerates this file from ../droste/bazel_deps.yaml.
def _jar_artifact_impl(ctx):
    jar_name = "%s.jar" % ctx.name
    ctx.download(
        output=ctx.path("jar/%s" % jar_name),
        url=ctx.attr.urls,
        sha256=ctx.attr.sha256,
        executable=False
    )
    src_name="%s-sources.jar" % ctx.name
    srcjar_attr=""
    has_sources = len(ctx.attr.src_urls) != 0
    if has_sources:
        ctx.download(
            output=ctx.path("jar/%s" % src_name),
            url=ctx.attr.src_urls,
            sha256=ctx.attr.src_sha256,
            executable=False
        )
        srcjar_attr ='\n    srcjar = ":%s",' % src_name

    build_file_contents = """
package(default_visibility = ['//visibility:public'])
java_import(
    name = 'jar',
    tags = ['maven_coordinates={artifact}'],
    jars = ['{jar_name}'],{srcjar_attr}
)
filegroup(
    name = 'file',
    srcs = [
        '{jar_name}',
        '{src_name}'
    ],
    visibility = ['//visibility:public']
)\n""".format(artifact = ctx.attr.artifact, jar_name = jar_name, src_name = src_name, srcjar_attr = srcjar_attr)
    ctx.file(ctx.path("jar/BUILD"), build_file_contents, False)
    return None

jar_artifact = repository_rule(
    attrs = {
        "artifact": attr.string(mandatory = True),
        "sha256": attr.string(mandatory = True),
        "urls": attr.string_list(mandatory = True),
        "src_sha256": attr.string(mandatory = False, default=""),
        "src_urls": attr.string_list(mandatory = False, default=[]),
    },
    implementation = _jar_artifact_impl
)

def jar_artifact_callback(hash):
    src_urls = []
    src_sha256 = ""
    source=hash.get("source", None)
    if source != None:
        src_urls = [source["url"]]
        src_sha256 = source["sha256"]
    jar_artifact(
        artifact = hash["artifact"],
        name = hash["name"],
        urls = [hash["url"]],
        sha256 = hash["sha256"],
        src_urls = src_urls,
        src_sha256 = src_sha256
    )
    native.bind(name = hash["bind"], actual = hash["actual"])


def list_dependencies():
    return [
    {"artifact": "org.spire-math:kind-projector_2.11:0.9.7", "lang": "scala", "sha1": "9dd7e57630f5a3765e7a65c08a02f83cecc59ff7", "sha256": "fe361768beedc59434652c96d9b990685460f853f2e0d609dfc81a16b0953fb4", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/spire-math/kind-projector_2.11/0.9.7/kind-projector_2.11-0.9.7.jar", "source": {"sha1": "2166bc8657e047df281c823b28c6495eae4fd993", "sha256": "6f92ccba8f027fbfaa21b4d69d1c05facb0f2e317f21f7d60f2c8244f9a27a3b", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/spire-math/kind-projector_2.11/0.9.7/kind-projector_2.11-0.9.7-sources.jar"} , "name": "io_higherkindness_droste_org_spire_math_kind_projector_2_11", "actual": "@io_higherkindness_droste_org_spire_math_kind_projector_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/spire_math/kind_projector_2_11"},
    {"artifact": "org.typelevel:cats-core_2.11:1.1.0", "lang": "scala", "sha1": "854ab2123eccb2edc7bf00a484cf7826626ce71d", "sha256": "ee02e8d0d9aee4670c5f607f6cb00f71c176664e2f199d9b5e0052b485656220", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-core_2.11/1.1.0/cats-core_2.11-1.1.0.jar", "source": {"sha1": "c4e6b4fc2eb6764c998fae76b279144dff934947", "sha256": "4a00c7cdcd573726c807c4b9cf28738de7c9d29f9d503bb9ba6234b2a7480a6c", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-core_2.11/1.1.0/cats-core_2.11-1.1.0-sources.jar"} , "name": "io_higherkindness_droste_org_typelevel_cats_core_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_core_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_core_2_11"},
    {"artifact": "org.typelevel:cats-free_2.11:1.1.0", "lang": "scala", "sha1": "a43effe7b5c1d9a1d1dd105865e4afe987e6e402", "sha256": "0179b700e944b2571bfe5d3c26fb83138eceedbc46d7b468863b9154d8ad1a24", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-free_2.11/1.1.0/cats-free_2.11-1.1.0.jar", "source": {"sha1": "003ef156694a3781f1ef31b64bcb8c3b2fbd20c3", "sha256": "910fdb00f6a7a522889899c9cd27035a378e6a20616c0a42eebff67e6eaa2407", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-free_2.11/1.1.0/cats-free_2.11-1.1.0-sources.jar"} , "name": "io_higherkindness_droste_org_typelevel_cats_free_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_free_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_free_2_11"},
    {"artifact": "org.typelevel:cats-kernel_2.11:1.1.0", "lang": "scala", "sha1": "45051dfb4703364929d603ac06afcda4a421d8da", "sha256": "a4cc83ea4d250b10f8e263f995662615c2395fb14ef073e864c97368d5cb8d96", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-kernel_2.11/1.1.0/cats-kernel_2.11-1.1.0.jar", "source": {"sha1": "cd59a9802a5e27ca4df5fd3a2e2f28a203320f94", "sha256": "b30a0aaad99a9ca84759a1d684051df3b162c3cf9117b04f75f05c611698ab4d", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-kernel_2.11/1.1.0/cats-kernel_2.11-1.1.0-sources.jar"} , "name": "io_higherkindness_droste_org_typelevel_cats_kernel_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_kernel_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_kernel_2_11"},
    {"artifact": "org.typelevel:cats-macros_2.11:1.1.0", "lang": "scala", "sha1": "59a4fa3d642046b3997cfcc2db43f4df736545f2", "sha256": "89dad2adaac43c7001f68e7fa38a077ff4cab36ac5c1bccbdbea399aae4f14ba", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-macros_2.11/1.1.0/cats-macros_2.11-1.1.0.jar", "source": {"sha1": "7adb73bea783050070386b89b3adce62040d4605", "sha256": "46914a99bcb652de60fc7e380041c270e2b099e579dce70de8d07e7c9c56ce56", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/cats-macros_2.11/1.1.0/cats-macros_2.11-1.1.0-sources.jar"} , "name": "io_higherkindness_droste_org_typelevel_cats_macros_2_11", "actual": "@io_higherkindness_droste_org_typelevel_cats_macros_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/cats_macros_2_11"},
    {"artifact": "org.typelevel:machinist_2.11:0.6.2", "lang": "scala", "sha1": "029c6a46d66b6616f8795a70753e6753975f42fc", "sha256": "44d11274e9cf1d6d22cd79a38abd60986041eb8a58083682df29bafd0aaba965", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/machinist_2.11/0.6.2/machinist_2.11-0.6.2.jar", "source": {"sha1": "98edae0ef106ad778b87080ef59df8ac08ab4d63", "sha256": "35b143492371211bcc5b9b0b0f4dc1d57ac63b79004124b9ebca7662215f5e89", "repository": "https://repo.maven.apache.org/maven2/", "url": "https://repo.maven.apache.org/maven2/org/typelevel/machinist_2.11/0.6.2/machinist_2.11-0.6.2-sources.jar"} , "name": "io_higherkindness_droste_org_typelevel_machinist_2_11", "actual": "@io_higherkindness_droste_org_typelevel_machinist_2_11//jar:file", "bind": "jar/io_higherkindness_droste_org/typelevel/machinist_2_11"},
    ]

def maven_dependencies(callback = jar_artifact_callback):
    for hash in list_dependencies():
        callback(hash)
