#!/bin/bash
# Abre la ventana grafica del simulador del procesador MIPS
set -e
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
exec java -cp out mips.VentanaSimulador
