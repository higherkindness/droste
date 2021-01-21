{ java ? "openjdk14" }:

let
  sources = import ./nix/sources.nix;
  pkgs = import sources.nixpkgs {};
  sbt = pkgs.sbt.override {
    jre = pkgs.${java};
  };
  metals = pkgs.metals.override {
    jre = pkgs.${java};
  };
in pkgs.mkShell {
  buildInputs = [
    pkgs.${java}
    pkgs.git
    pkgs.jekyll
    metals
    pkgs.nodejs
    sbt
    pkgs.perl
    pkgs.niv
  ];
}
