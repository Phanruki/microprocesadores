#!/bin/bash
# Compila el emulador y las pruebas
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out $(find src -name '*.java')
echo "Compilacion OK -> out/"
