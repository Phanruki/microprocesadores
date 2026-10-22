#!/bin/bash
# Ejecuta los 5 programas de usuario final y genera capturas inicio/fin
set -e
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
mkdir -p salidas
for p in sumatoria factorial integral promedio perceptron; do
  echo "--- $p ---"
  java -cp out ui.Simulador "$p" --estado --captura "salidas/${p}_captura.txt"
  echo
done
echo "Capturas guardadas en salidas/"
