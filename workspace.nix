{ pkgs ? import <nixpkgs> {} }:
with pkgs;

stdenv.mkDerivation rec {
  name = "env";

  src = builtins.filterSource (path: type: false) ./.;
  
  bazel = (import (fetchFromGitHub {
    owner = "andyscott";
    repo = "bazel-nix";
    rev = "d03cebdba5aefdccad70608ec348fdd1ca0c1ed1";
    sha256 = "08znczxmm4ijlw3lfcz8xw3f1368nn56i5ka453gygslj6qfg1g3";
  })) {
    version = "0.14.1";
  };

  buildInputs = [
    sbt nodejs bazel
  ];

  installPhase = ''
    mkdir -p $out
    ln -s $bazel/bin/bazel $out/bazel
  '';
}

