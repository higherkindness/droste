#!/bin/bash

echo -ne "\033[0;32m"
echo 'Updating jvm dependencies. This will take a few minutes.'
echo -ne "\033[0m"

# update this to move to later versions of this repo:
# https://github.com/johnynek/bazel-deps
GITSHA="18928d33855defd5a72078938231ea1ef022c924"

set -e

SCRIPTS_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $SCRIPTS_DIR

REPO_ROOT=$(git rev-parse --show-toplevel)

BAZEL_DEPS_PATH="$HOME/.bazel-deps-cache/$(basename $REPO_ROOT)"
BAZEL_DEPS_REPO_PATH="$BAZEL_DEPS_PATH/bazel-deps"
BAZEL_DEPS_WORKSPACE="$BAZEL_DEPS_REPO_PATH/WORKSPACE"

if [ ! -f "$BAZEL_DEPS_WORKSPACE" ]; then
  mkdir -p $BAZEL_DEPS_PATH
  cd $BAZEL_DEPS_PATH
  git clone https://github.com/johnynek/bazel-deps.git
fi

cd $BAZEL_DEPS_REPO_PATH
git reset --hard $GITSHA || (git fetch && git reset --hard $GITSHA)
bazel run //:parse -- generate -r $REPO_ROOT -s 3rdparty/workspace.bzl -d bazel_deps.yaml
# now reformat the dependencies to keep them sorted
bazel run //:parse -- format-deps -d $REPO_ROOT/bazel_deps.yaml -o
