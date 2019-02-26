{ pkgs ? import <nixpkgs> {} }:
with pkgs;

stdenv.mkDerivation rec {
  name = "env";

  src = builtins.filterSource (path: type: false) ./.;

  buildInputs = [
    git
    nodejs
    openjdk8
    perl
    sbt
  ];
}
