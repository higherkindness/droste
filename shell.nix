{ java ? "openjdk11" }:

let
  sources = import ./nix/sources.nix;
  pkgs = import sources.nixpkgs {};
in
pkgs.mkShell {
  buildInputs = [
    pkgs.${java}
    pkgs.git
    pkgs.jekyll
    pkgs.metals
    pkgs.nodejs
    pkgs.openjdk8
    pkgs.perl
    pkgs.sbt
    pkgs.niv
  ];
}
