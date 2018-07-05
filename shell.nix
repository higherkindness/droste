{ pkgs ? import <nixpkgs> {} }:

with pkgs;

stdenv.mkDerivation {
  name = "droste";
  buildInputs = [ sbt nodejs ];
}
