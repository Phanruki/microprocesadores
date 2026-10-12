#!/bin/bash
# Ejecuta el plan completo de pruebas del simulador MIPS
set -e
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
java -cp out pruebas.EjecutorPruebas | tee salidas/plan_pruebas_completo.txt
