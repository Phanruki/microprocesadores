#!/usr/bin/env python3
"""
Genera las imágenes de "capturas de pantalla" del reporte final a partir de las
salidas REALES que produce el emulador (salidas/*_captura.txt).

No se inventa nada: el texto de cada imagen es exactamente el que imprime
`java -cp out ui.Emulador <programa> --estado --captura ...`; sólo se pinta con
aspecto de terminal para insertarlo en el PDF (sección "Evidencia de ejecución").

Uso (desde la raíz del proyecto):
    ./compilar.sh && ./ejecutar_programas.sh && ./probar.sh
    python3 docs/reporte_exposicion/reporte_final/generar_capturas.py

Salida: docs/reporte_exposicion/reporte_final/imagenes/ev_*.png
"""

import os
import re
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
SALIDAS = os.path.join(RAIZ, "salidas")
IMAGENES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "imagenes")

PROGRAMAS = ["sumatoria", "factorial", "integral", "promedio", "perceptron"]

FONDOS = "#1E222B"          # fondo de la "terminal"
BARRA = "#31374A"           # barra de título
TEXTO = "#E6E9EF"           # texto normal
DESTACADO = "#8FD48F"       # resultados / líneas de interés
APAGADO = "#8A90A0"         # texto secundario

FUENTES = [
    "/usr/share/fonts/google-noto-vf/NotoSansMono[wght].ttf",
    "/usr/share/fonts/urw-base35/NimbusMonoPS-Regular.otf",
]


def fuente(tam):
    for ruta in FUENTES:
        if os.path.exists(ruta):
            try:
                return ImageFont.truetype(ruta, tam)
            except Exception:
                continue
    return ImageFont.load_default()


def terminal(imagen, titulo, lineas, destacar=()):
    """Dibuja una 'terminal' con las líneas dadas y la guarda en `imagen`."""
    tam, alto_linea, margen = 16, 25, 18
    f_txt, f_tit = fuente(tam), fuente(15)

    medidas = ImageDraw.Draw(Image.new("RGB", (10, 10)))
    ancho_txt = max((medidas.textlength(l, font=f_txt) for l in lineas), default=400)
    ancho_tit = medidas.textlength(titulo, font=f_tit)
    ancho = int(max(ancho_txt, ancho_tit) + 2 * margen)
    alto = 38 + len(lineas) * alto_linea + 2 * margen

    img = Image.new("RGB", (ancho, alto), FONDOS)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, ancho, 38], fill=BARRA)
    for i, c in enumerate("#E86A5E #E8C15E #8FD48F".split()):   # semáforo de la ventana
        d.ellipse([14 + i * 22, 13, 28 + i * 22, 27], fill=c)
    d.text((90, 10), titulo, font=f_tit, fill="#C9CFDD")

    y = 38 + margen
    for linea in lineas:
        color = DESTACADO if any(k in linea for k in destacar) else TEXTO
        d.text((margen, y), linea, font=f_txt, fill=color)
        y += alto_linea

    img.save(imagen)
    print("  ", os.path.relpath(imagen, RAIZ))


def seccion_inicio(lineas):
    """Primeras líneas: encabezado, mapa de memoria y estado inicial."""
    fin = next((i for i, l in enumerate(lineas) if "=== REGISTROS" in l), 14)
    return lineas[: fin + 5]


def seccion_fin(lineas):
    """Salida del programa + FIN + registros con valor + PC/HI-LO + primeras celdas RAM."""
    ini = next((i for i, l in enumerate(lineas) if "SALIDA DEL PROGRAMA" in l), 0)
    fin = next((i for i, l in enumerate(lineas) if "ESTADO FINAL" in l), len(lineas))
    bloque = lineas[ini:fin]

    registros = [l for l in lineas[fin:] if re.match(r"\s*\d+ \$", l) and not l.rstrip().endswith("(0x00000000)")]
    pc_hi_lo = [l for l in lineas[fin:] if l.startswith("PC =") or l.startswith("HI=")]
    ram = [l for l in lineas[fin:] if l.strip().startswith("[") and "dir 1" in l][:6]

    return (bloque + ["", "ESTADO FINAL (registros con valor):"] + registros
            + pc_hi_lo + ["", "Primeras celdas de la RAM (datos):"] + ram)


def main():
    os.makedirs(IMAGENES, exist_ok=True)
    print("Generando capturas de la sección de evidencia:")

    for p in PROGRAMAS:
        ruta = os.path.join(SALIDAS, f"{p}_captura.txt")
        if not os.path.exists(ruta):
            print("  FALTA", ruta, "- ejecuta ./ejecutar_programas.sh", file=sys.stderr)
            continue
        lineas = [l.rstrip("\n") for l in open(ruta, encoding="utf-8")]
        cmd = f"$ java -cp out ui.Emulador {p} --estado --captura salidas/{p}_captura.txt"
        destacar = ("=", "FIN DE EJECUCION")

        terminal(os.path.join(IMAGENES, f"ev_{p}_inicio.png"),
                 f"terminal — {p} (inicio de la ejecución)",
                 [cmd, ""] + seccion_inicio(lineas), destacar)

        terminal(os.path.join(IMAGENES, f"ev_{p}_fin.png"),
                 f"terminal — {p} (fin de la ejecución)",
                 [cmd, ""] + seccion_fin(lineas), destacar)

    # Traza del factorial (PC + código en hexadecimal, tal como la imprime --traza)
    out = subprocess.run(["java", "-cp", "out", "ui.Emulador", "factorial", "--traza"],
                         cwd=RAIZ, capture_output=True, text=True).stdout.splitlines()
    ini = next(i for i, l in enumerate(out) if "SALIDA DEL PROGRAMA" in l)
    terminal(os.path.join(IMAGENES, "ev_traza_factorial.png"),
             "terminal — factorial --traza (primeros ciclos)",
             ["$ java -cp out ui.Emulador factorial --traza", ""]
             + [l for l in out[ini:ini + 26] if l.strip()],
             ("Factorial", "=", "FIN"))

    # Resumen de las 201 pruebas automáticas
    out = subprocess.run(["java", "-cp", "out", "pruebas.EjecutorPruebas"],
                         cwd=RAIZ, capture_output=True, text=True).stdout.splitlines()
    resumen = [l for l in out if "RESUMEN" in l or "Pasadas" in l or "Resultado global" in l]
    terminal(os.path.join(IMAGENES, "ev_pruebas.png"),
             "terminal — pruebas automáticas (201)",
             ["$ java -cp out pruebas.EjecutorPruebas", ""] + resumen,
             ("Pasadas", "Resultado global"))

    print("Listo.")


if __name__ == "__main__":
    main()
