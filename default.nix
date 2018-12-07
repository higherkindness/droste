{ pkgs ? import <nixpkgs> {} }:
with pkgs;

stdenv.mkDerivation rec {
  name = "env";

  src = builtins.filterSource (path: type: false) ./.;

  buildInputs = [
    sbt bazel nodejs git openjdk8
  ];

  shellHook =
    ''
    export IS_IN_NIX=true
    '';
}
