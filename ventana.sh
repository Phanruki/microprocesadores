#!/bin/bash
set -e
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
exec java -cp out ui.VentanaSimulador
