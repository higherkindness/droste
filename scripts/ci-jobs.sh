#! /usr/bin/env nix-shell
#! nix-shell ../shell.nix -i bash

set -euxo pipefail

case "$1" in
    "bazel")
        ./tools/bazel build ...
        ;;
    "test")
        sbt ';+clean;+test'
        ;;
    "format")
        ./scalafmt --test
        ;;
    "coverage")
        sbt 'project coverage' test coverageReport
        bash <(curl -s https://codecov.io/bash)
        ;;
    "readme")
        sbt 'project readme' tut
        git diff --exit-code -- *.md
        ;;
    *)
        echo "no command specified!"
        exit 1
        ;;
esac
