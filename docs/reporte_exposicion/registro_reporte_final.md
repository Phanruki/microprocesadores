# Registro de información para el reporte final

> Este documento **no es el reporte final**: es todo lo acumulado para
> redactarlo después (donde sea necesario copiar/pegar y editar). El reporte
> final se entrega como **PDF por Eminus** (nunca por otro medio) usando la
> **plantilla UV**.

Requisitos del reporte (de `docs/practica/reporte_final.txt`):

1. Portada: nombres, matrículas, título, fecha de entrega + plantilla UV.
2. Introducción: (a) razones para desarrollar un procesador propio desde cero,
   (b) ganancia de un alumno que sabe implementar un emulador de un procesador
   real, (c) por qué se implementó un emulador para el procesador MIPS.
3. Evidencia de ejecución: tabla "Línea de código a ejecutar | resultado del
   emulador".
4. Uso de IA: incluir los PROMPTs.
5. Reflexiones finales: nivel de programación requerido, trabajo en equipo, y
   qué le conviene a los que cursan el año próximo.
6. Axiológico (libertad, responsabilidad, solidaridad, respeto, honestidad,
   integridad) e heurístico (cumplir las instrucciones de la práctica).

---

## 0. Verificación contra la práctica y el diagrama del compañero (2026-10-22)

> Todo lo de esta sección se **comprobó ejecutando** el proyecto hoy
> (`./compilar.sh`, `java -cp out pruebas.EjecutorPruebas`, lectura de
> `salidas/`, lectura de `docs/practica/practica.txt` + `reporte_final.txt`
> y revisión de `docs/practica/DiagramaDiego.jpg`). Es material de apoyo para
> las secciones 2, 3 y 5.

### 0.1 Checklist de la práctica (Pasos 1 a 5)

| Paso | Qué pide el profesor | Dónde está en el proyecto | Estado |
|---|---|---|---|
| 1 | Ancho de palabra = 32 bits | `UnidadAlmacenamiento.ANCHO_PALABRA_DEFECTO = 32`; `ROM/RAM/DiscoDuro.ANCHO_PALABRA = 32` | ✅ |
| 1 | Registros del procesador | `Registros.CANTIDAD = 32`, `Registros.ANCHO = 32` | ✅ |
| 1 | PC de 32 bits que se incrementa al buscar | `cpu/PC.ANCHO = 32`, `incrementar()`, `reiniciar()` → 0 | ✅ |
| 1 | `DiscoDuro` = copia de "unidad de almacenamiento", 32 bits, 100 celdas | `DiscoDuro extends UnidadAlmacenamiento`, `CELDAS = 100`, direcciones 200..299 | ✅ |
| 1 | RAM 32 bits / 100 celdas | `RAM.CELDAS = 100`, `DIRECCION_BASE = 100` (100..199) | ✅ |
| 1 | ROM 32 bits / 100 celdas | `ROM.CELDAS = 100`, `DIRECCION_BASE = 0` (0..99) | ✅ |
| 2 | Seleccionar procesador a emular → **MIPS** | casilla `[ x ] MIPS` en `practica.md`; código con formatos R/I/J | ✅ |
| 3 | 3 planes de pruebas (técnicas, integración, sistema) | `docs/pruebas/plan_pruebas_{unitarias,integracion,sistema}.md` + `src/pruebas/*` | ✅ |
| 4 | 5 programas de usuario (sumatoria, factorial, integral, promedio, perceptrón) | `src/programas/{Sumatoria,Factorial,Integral,Promedio,Perceptron}.java` | ✅ |
| 4 | Capturas al inicio y al final de cada ejecución | `salidas/*_captura.txt` (cada una con línea `INICIO DE EJECUCION` y `FIN DE EJECUCION`; 188–189 líneas) | ✅ texto; falta confirmar si exige imágenes (ver pendientes, sección 7) |
| 5 | Reporte con portada/introducción/evidencia/IA/reflexiones en PDF por Eminus | material en `docs/reporte_exposicion/` (este registro + `prompts_usados.md`) | ⏳ en proceso |

### 0.2 Salidas reales re-verificadas hoy (para la tabla de evidencia)

`./compilar.sh` → `Compilacion OK -> out/`
`java -cp out pruebas.EjecutorPruebas` → **117 unitarias + 44 integración + 40 sistema = 201 pruebas, 0 fallidas** (`Resultado global: todas las pruebas pasaron`).

| Programa | Salida real | Ciclos (línea `FIN DE EJECUCION`) | Captura |
|---|---|---|---|
| `sumatoria` | `Sumatoria de 3 a 10 = 52` | 68 | `salidas/sumatoria_captura.txt` |
| `factorial` | `Factorial de 7 = 5040` | 63 | `salidas/factorial_captura.txt` |
| `integral` | `Integral de x^2 dx desde x=1 hasta x=5 = 30` | 56 | `salidas/integral_captura.txt` |
| `promedio` | `Vector: 4 8 15 16 23` / `Promedio = 13` | 123 | `salidas/promedio_captura.txt` |
| `perceptron` | `z = 3  y = 1` | 62 | `salidas/perceptron_captura.txt` |

Primeras líneas reales de la traza de `factorial` (`--traza`, se imprime en
hexadecimal; la interpretación en ensamblador es la de la Tabla 1 del borrador,
que coincide palabra por palabra):

```
  [   0] PC=  0  0x8C080064   -> lw   $t0, 100($zero)
  [   1] PC=  1  0x24020004   -> addiu $v0, $zero, 4
  [   2] PC=  2  0x24040065   -> addiu $a0, $zero, 101
  [   3] PC=  3  0x0000000C   -> syscall
```

### 0.3 Correspondencia con el diagrama del compañero (`docs/practica/DiagramaDiego.jpg`)

Diagrama de Diego: `com > mycompany > mipsemulator > { ui, cpu, execution,
memory, alu, instruction }`.

| Paquete/clase en el diagrama de Diego | Equivalente en nuestro proyecto | Observación |
|---|---|---|
| `cpu`: `CPU`, `PC` | `cpu`: `CPU`, `PC` | **Idéntico** |
| `execution`: `EjecutorInstrucciones`, `UnidadControl` | `ejecucion`: `EjecutorInstrucciones`, `UnidadControl`, **`SenalesControl`** | mismo esqueleto + añadimos las señales de control (con `Builder`) |
| `memory`: `Registros`, `MemoriaInstrucciones`, `DiscoDuro`, `ROM`, `RAM`, `UnidadAlmacenamiento` | `memoria`: `Registros`, **`MemoriaSistema`**, `DiscoDuro`, `ROM`, `RAM`, `UnidadAlmacenamiento` | misma jerarquía de herencia (`ROM/RAM/DiscoDuro --|> UnidadAlmacenamiento`); nosotros usamos `MemoriaSistema` (enrutador por dirección) en lugar de `MemoriaInstrucciones` |
| `alu`: `ALU` | `alu`: `ALU` + enum `OperacionALU` | idéntico + explícito el enum |
| `instruction`: `Instruccion`, `Decodificador`, `FormatoInstruccion`«E», `Funct`«E», `Opcode`«E» | `instruccion`: los mismos + `ServicioSyscall`, `ProgramaBuilder`, `Desensamblador`, `Binario` | misma base + herramientas de apoyo |
| `ui`: `MIPSEmulatorUI` | `ui`: `Emulador` (texto) y `VentanaEmulador` (Swing) | equivalente funcional |
| `Microprocesador` (clase raíz) | no tiene equivalente literal | el papel lo cumple `CPU`; `Emulador`/`VentanaEmulador` la orquestan |
| contenedor `com.mycompany.mipsemulator` | paquetes en la raíz de `src/` | sin prefijo de empresa (proyecto escolar) |
| (no existe) | `ui`, `programas`, `pruebas` como clientes del núcleo | lo muestra `docs/diagramas/diagrama_clases_exterior.png` |

Nuestros diagramas: `docs/diagramas/diagrama_clases_nucleo.png` (los 5 paquetes,
el "equivalente" al de Diego) y `diagrama_clases_exterior.png`; regenerables con
PlantUML según `docs/diagramas/generacion_diagrama_clases.md`.

**Conclusión para el reporte**: mismo esqueleto de 5 unidades
(cpu / ejecución / memoria / ALU / instrucción); lo que aportamos por encima del
diagrama del compañero son `SenalesControl`, `MemoriaSistema` con direcciones
visibles, `ProgramaBuilder`/`Desensamblador`/`ServicioSyscall`, la instrucción
`HALT` (0x3F) y la CPU que nunca muere (PC circular + NOP en 0 + límite de
100000 ciclos). Útil para la reflexión de "trabajo en equipo" y para la pregunta
probable **P13** de `preparacion_exposicion.md`.

### 0.4 Material del reporte disponible en `docs/`

Todo el material del reporte vive ahora en la documentación del proyecto
(la carpeta `entregable_escuela/` y su borrador `.docx` **ya no se usan**):

| Archivo | Qué contiene |
|---|---|
| `docs/reporte_exposicion/registro_reporte_final.md` (este archivo) | datos de portada, material de la introducción, tablas de evidencia, reflexiones y pendientes |
| `docs/reporte_exposicion/prompts_usados.md` | los **13 prompts** completos de la sección "Uso de IA" (con autor, texto y resultado; el prompt de `DiscoDuro` se excluyó del reporte) |
| `docs/reporte_exposicion/preparacion_exposicion.md` | explicación para exponer + preguntas probables del profesor |
| `docs/arquitectura.md`, `docs/instrucciones.md` | arquitectura y conjunto de instrucciones |
| `docs/pruebas/plan_pruebas_*.md` | los 3 planes de pruebas |
| `docs/programas/factorial_explicacion.md` | explicación línea por línea del factorial |
| `docs/diagramas/*.png` / `.puml` | diagramas de clases regenerados |
| `docs/practica/final/plantillas/Reporte_LaTeX/main.tex` (+ `imagenes/`) | plantilla de reporte UV en LaTeX (proyecto "NetPro") → **base copiada y modificada** para `reporte_final.tex` |
| `docs/practica/final/plantillas/presentacion_plantilla.pptx` | **ejemplo** de presentación UV usada en un curso anterior → referencia de estructura |
| `docs/practica/ejemplo_expo.pdf`, `docs/practica/DiagramaDiego.jpg` | ejemplos de referencia de compañeros |
| **`docs/reporte_exposicion/reporte_final/reporte_final.tex`** | **el reporte final** (único archivo): copia modificada de la plantilla UV `Reporte_LaTeX/main.tex` (mismo estilo: colores, tipografía, encabezados y pies) |
| `docs/reporte_exposicion/reporte_final/reporte_final.pdf` | PDF compilado con XeLaTeX (61 páginas) — el `.docx` anterior se eliminó por no seguir el formato |
| `docs/reporte_exposicion/reporte_final/imagenes/ev_*.png` | 12 figuras de evidencia (inicio/fin de cada programa, traza y pruebas) |
| `docs/reporte_exposicion/reporte_final/imagenes/ia/*.png` | **34 capturas de las conversaciones de IA** (los 13 prompts, renombradas `pNN_tema_M.png`) |
| `docs/reporte_exposicion/reporte_final/generar_capturas.py` | convierte `salidas/*_captura.txt` en las 12 imágenes de terminal |

Datos clave que debe reflejar el reporte (verificados, secciones 0.1 y 0.2):
estructura de **5 paquetes**, **201 pruebas** (117 + 44 + 40), ciclo de
**5 etapas** (IF/ID/EX/MEM/WB), ALU con **13 operaciones + NOP**, instrucción
**HALT** (0x3F), CPU que **nunca muere** (PC circular, NOP en 0, límite de
100000 ciclos) y los ciclos reales **68 / 63 / 56 / 123 / 62**.

---

## 1. Datos de la portada

- Facultad: **Facultad de Ingeniería en Electrónica y Comunicaciones — Región Poza Rica-Tuxpan**
- Licenciatura: **Ingeniería en Tecnologías Computacionales**
- Trabajo: **Práctica 2 — "Emulador Básico de un Microprocesador"**
  (**terminología**: en todo el proyecto, el código y la documentación se usa
  **"emulador"**, igual que la práctica y su rúbrica; el texto del profesor
  `practica.md` (copia de la práctica) es el único lugar donde aparece
  "simulador" y no se modificó porque es un archivo del profesor).
- Equipo (datos del equipo):

  | Nombre | Matrícula |
  |---|---|
  | Yadid Asiel Soto Hernández | zS24006887 |
  | Jose de Jesus Berman Islas | zS24006897 |
  | Miguel Alejandro Damian Bumas | zS24021396 |

- Docente: **Dr. Raúl Varguez Fernández**
- Fecha de entrega: **pendiente de fijar** (colocar el mes/año reales de la
  entrega en Eminus).
- Lema UV (portada y pie): **"Lis de Veracruz: Arte, Ciencia, Luz"** (+ `www.uv.mx`).
- Plantilla UV para el reporte: la práctica dice descargarla de **Eminus**
  ("Descargar plantilla UV"). El documento final se entrega **únicamente en PDF
  por Eminus**. Como **referencias de estilo UV** quedaron en el proyecto:
  - `docs/practica/final/plantillas/Reporte_LaTeX/main.tex` (+ `imagenes/`):
    **reporte completo** de otro proyecto ("NetPro") en LaTeX con estética UV
    (portada UV, `logo_uv.png`, `deco_uv.png`, índice, figuras, tablas);
    compila con **XeLaTeX** (no instalado aquí → Overleaf o instalar TeX Live).
  - `docs/practica/final/plantillas/presentacion_plantilla.pptx`: **presentación
    ya usada** en un curso anterior (Protocolo de Investigación, sept. 2025,
    Soto Hernández/Del Ángel) → referencia de estructura de portada UV.
  - `docs/practica/ejemplo_expo.pdf`: ejemplo de exposición de un compañero.
- Secciones que debe tener el documento (según la práctica): **Portada ·
  Introducción · Evidencia de ejecución del código · Uso de inteligencias
  artificiales (prompts) · Reflexiones finales** (+ referencias, opcional).
- Reparto del equipo (dato para la reflexión de "trabajo en equipo"):
  *Yadid* → unidades de almacenamiento + ALU, programas `sumatoria` y
  `promedio`; *José de Jesús* → sistema de pruebas, documentación y `DiscoDuro`;
  *Miguel* → programas `factorial`, `integral`, `perceptron` y la ventana
  gráfica. (Revalidar tras la reestructuración a 5 paquetes.)

---

## 2. Información de la introducción (material para redactar)

### 2.1 ¿Por qué construir un procesador desde cero?

- El procesador es la unidad que realmente ejecuta el programa: sin entenderlo,
  programar es "dar órdenes a una caja negra".
- Construirlo desde cero obliga a comprender qué pasa entre el `x = a + b` del
  lenguaje y los bits que circulan por registros, ALU y memoria.
- Se entiende el **ciclo de instrucción** (fetch-decode-execute), los formatos
  de instrucción fijos de RISC y por qué un programa es una secuencia de
  palabras de 32 bits en memoria.
- Permite ver el hardware y el software como el mismo sistema: el compilador,
  el ensamblador y el sistema operativo existen porque la CPU hace exactamente
  esto.

### 2.2 ¿Qué gana el alumno al saber implementar un emulador de un procesador real?

- Depuración de bajo nivel: cuando el programa "no funciona" sin razón aparente,
  se sabe mirar registros, memoria y el flujo de instrucciones.
- Rendimiento: se comprenden desbordamiento, extensiones de signo, desplazamientos,
  alineación y por qué `addu`/`addi` se comportan distinto.
- Seguridad y robustez: los desbordamientos y los bucles infinitos dejan de ser
  magia.
- Base para cursos posteriores: sistemas operativos, compiladores, arquitectura
  de computadoras y hasta FPGA/microcontroladores.
- Disciplina de diseño: modularidad (paquetes), contratos entre unidades
  (señales de control) y pruebas automatizadas.

### 2.3 ¿Por qué MIPS y no otro procesador?

- MIPS es **RISC** con **3 formatos de instrucción fijos** (R, I y J): la
  decodificación es simple y regular, ideal para un emulador educativo.
- El conjunto es pequeño pero completo: aritméticas, lógicas, desplazamientos,
  carga/escritura, ramas y saltos.
- Su documentación es abierta y abundante; x86 (CISC, formatos variables y
  microcódigo) sería desproporcionado para una práctica.
- Los mismos conceptos (PC, registros, ALU, memoria, ciclo de instrucción) se
  trasladan después a RISC-V, ARM o x86.

---

## 3. Evidencia de ejecución: tabla línea de código → resultado

La tabla del reporte pide "Línea de código a ejecutar" y "resultado del
emulador". Se puede armar con dos tipos de fila: **comandos de consola** y
**código/trazas del emulador**. Todo lo de abajo está verificado en este
proyecto (`salidas/`).

### 3.1 Filas con comandos de consola

| Línea de código a ejecutar | Resultado del emulador |
|---|---|
| `./compilar.sh` | `Compilacion OK -> out/` |
| `java -cp out ui.Emulador sumatoria --estado --captura salidas/sumatoria_captura.txt` | `Sumatoria de 3 a 10 = 52` y `FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 68` |
| `java -cp out ui.Emulador factorial` | `Factorial de 7 = 5040` (ciclos: 63) |
| `java -cp out ui.Emulador integral` | `Integral de x^2 dx desde x=1 hasta x=5 = 30` (ciclos: 56) |
| `java -cp out ui.Emulador promedio` | `Vector: 4 8 15 16 23` / `Promedio = 13` (ciclos: 123) |
| `java -cp out ui.Emulador perceptron` | `z = 3  y = 1` (ciclos: 62) |
| `java -cp out pruebas.PruebasUnitarias` | `RESUMEN PRUEBAS UNITARIAS / Pasadas: 117  Fallidas: 0` |
| `java -cp out pruebas.PruebasIntegracion` | `RESUMEN PRUEBAS DE INTEGRACION / Pasadas: 44  Fallidas: 0` |
| `java -cp out pruebas.PruebasSistema` | `RESUMEN PRUEBAS DE SISTEMA / Pasadas: 40  Fallidas: 0` |
| `java -cp out pruebas.EjecutorPruebas` | `Resultado global: todas las pruebas pasaron` (201 pruebas) |
| `java -cp out ui.Emulador factorial --traza --estado` | traza por ciclo con formato `[  n] PC= x  0xXXXXXXXX` (número de ciclo, PC y código de la instrucción en **hexadecimal**) + volcado de registros y mapa de memoria. **Nota verificada**: el mnemónico/ensamblador y las señales de control **no** aparecen en `--traza`, sino en `ui.VentanaEmulador` (usa `Desensamblador.mnemonico()` / `.operacion()`) |

### 3.2 Filas con contenido del emulador (ejemplo de traza)

Ejemplo real de lo que muestra `--traza` (añadir 3–5 filas en el reporte con
la salida real del programa que más guste):

| Línea de código a ejecutar | Resultado del emulador |
|---|---|
| Instrucción `addiu $t0, $zero, 7` (ciclo N) | `$t0 = 7 (0x00000007)`; PC avanza a la siguiente celda |
| Instrucción `mult $t0, $t1` | `LO = resultado`, `HI = 0` |
| Instrucción `mflo $t2` | `$t2` recibe el producto |
| Instrucción `lw $t3, 100($zero)` | lee la celda 100 (RAM, segmento "datos") |
| Instrucción `sw $t3, 101($zero)` | escribe en la celda 101; el volcado la muestra con dirección decimal y hexadecimal |
| Instrucción `halt` | `FIN DE EJECUCION - la CPU se detuvo con HALT/exit` |

### 3.3 Volcados de memoria (evidencia "000" y segmentos)

Cada captura de `salidas/` incluye:

- El mapa de segmentos:
  `texto 0..99 (0x00000000..0x00000063) ROM / datos 100..199 (0x00000064..0x000000C7) RAM / disco 200..299 (0x000000C8..0x0000012B) DiscoDuro`
- Estado inicial con todos los registros en `0 (0x00000000)`.
- El volcado completo de las celdas con dirección decimal y hexadecimal; la
  RAM arranca en `000`, p. ej. `[ 81] dir 181 (0x000000B5) = 0 (0x00000000)`.
- Estado final con los registros que quedaron con valores.

### 3.4 Evidencia de la ventana gráfica (`ui.VentanaEmulador`)

- Historial por ciclo: `Ciclo`, `PC`, instrucción en binario + ensamblador,
  operación, **señales de control** y `PC Siguiente`.
- Paneles de Registros, Memoria (por segmentos) y Estado CPU (PC, HI, LO).

---

## 4. Uso de IA: PROMPTs (declarar plenamente; el rubro lo exige)

La práctica pide declarar el uso de IA con sus prompts. **Decisión del equipo:
el rubro aplicable es "Uso de IA"** (según `docs/practica/reporte_final.txt`:
*"el equipo reconoce y describe plenamente el uso de IA"*), no el de CACHÉton
que aparece en una copia de la práctica.

Los **13 prompts** completos (con autor, texto y resultado) están en
**`docs/reporte_exposicion/prompts_usados.md`**; ese archivo es el que se copia
al reporte. (El prompt 11 original, sobre `DiscoDuro`, **se excluyó del
reporte** por decisión del equipo: no se conservan sus capturas.) Resumen:

| # | Autor | Tema |
|---|---|---|
| 1 | Yadid | Análisis de las clases y reorganización en paquetes |
| 2 | José de Jesús | Plan de pruebas unitarias (117) |
| 3 | José de Jesús | Plan de pruebas de integración (44) |
| 4 | José de Jesús | Plan de pruebas de sistema (40) |
| 5 | Miguel | Factorial (prompt literal de la práctica) |
| 6 | Yadid | Sumatoria 3→10 |
| 7 | Miguel | Integral de x² de 1 a 5 |
| 8 | Yadid | Promedio de {4, 8, 15, 16, 23} |
| 9 | Miguel | Perceptrón con entradas fijas |
| 10 | Yadid | Clase ALU |
| 11 | Miguel | Ventana gráfica (`ui.VentanaEmulador`) |
| 12 | José de Jesús | Documentación del conjunto de instrucciones |
| 13 | Equipo | Reestructuración final a 5 paquetes + HALT + CPU que nunca muere |

**Capturas de pantalla de los prompts**: el equipo pegó las 34 capturas de las
conversaciones en `docs/practica/final/capturas/` (originales, nombres
`Pasted image (N).png`); ya están identificadas una por una, copiadas y
renombradas en `docs/reporte_exposicion/reporte_final/imagenes/ia/` como
`pNN_tema_M.png` y **insertadas dentro del propio `reporte_final.tex`** en su
subsección correspondiente.

**Reflexión sobre IA para el reporte**: la IA se usó como copiloto (diseño de
la estructura de paquetes, generación de pruebas y redacción de explicaciones),
pero las decisiones (MIPS, HALT propio, segmentos de 100 celdas, límite de
ciclos) y la validación (correr las 201 pruebas y las capturas) las tomó y
verificó el equipo. La IA no sustituye ejecutar y comprobar: varias sugerencias
iniciales fallaban al compilar y se corrigieron (p. ej. las señales de control
se reconstruyeron con un Builder para evitar errores de argumentos posicionales).

---

## 5. Material para las reflexiones finales (redactar con voz propia)

### 5.1 Nivel de programación requerido

- Java "intermedio-alto": clases y herencia (`UnidadAlmacenamiento` ←
  `ROM`/`RAM`/`DiscoDuro`), enums (`Opcode`, `Funct`, `OperacionALU`),
  enums anidados con Builder (`SenalesControl.Builder`), `static`, arrays,
  listas, manejo de excepciones y E/S de archivos (disco duro).
- Poco o nada de "algoritmos raros": lo difícil no es la matemática sino el
  **diseño**: que cada unidad tenga una responsabilidad clara y que el ciclo de
  la CPU solo orqueste etapas.
- Pruebas automatizadas desde el día 1: sin ellas, un cambio en la ALU rompe
  los programas de usuario sin aviso.

### 5.2 Trabajo en equipo en el diseño e implementación

- El diseño (5 paquetes, mapa segmentado, señales de control) es un acuerdo
  entre todos: si uno cambia el contrato, se rompe todo. Se practicó
  comparando diagramas (el del compañero vs. el nuestro) y unificando.
- Reparto natural: quien domina memoria trabaja en `memoria`, quien domina
  flujo en `ejecucion`/`cpu`, etc.; las pruebas son el idioma común.
- Los diagramas y esta documentación son la memoria del equipo: sin ellos,
  retomar el código en dos semanas cuesta el doble.

### 5.3 ¿Por qué les conviene a los del próximo año?

- Es la práctica donde se ve **de verdad** qué es un procesador: quien la
  entiende, programa mejor en cualquier lenguaje (sabe qué cuesta cada
  operación).
- Abre puertas: sistemas operativos, compiladores, seguridad (desbordamientos),
  embebidos/IoT, FPGA y hasta inteligencia artificial en hardware.
- Aprenderán a depurar mirando el estado real (registros, memoria, PC) y no
  "probando a ver qué pasa", que es la diferencia entre un programador y otro.
- Y sobre todo: a documentar y a trabajar en equipo, que es lo que pedirá el
  mercado laboral.

### 5.4 Axiológico (para la sección de valores)

- **Libertad**: se eligió MIPS y un diseño propio dentro de los márgenes de la
  práctica.
- **Responsabilidad**: cada cambio se validó con las 201 pruebas antes de
  aceptarse.
- **Solidaridad**: documentación en español y diagramas para que cualquiera del
  equipo (o del curso) pueda seguir el código.
- **Respeto**: se respetó el enfoque del compañero comparando diagramas en vez
  de descartarlo.
- **Honestidad**: se declara el uso de IA con sus prompts; los resultados
  mostrados son salidas reales de `salidas/`, no inventadas.
- **Integridad**: el código cumple lo pedido por el profesor aunque haya
  obligado a tocar casi todo el proyecto.

---

## 6. Comandos y resultados clave (copiar en el reporte)

```bash
./compilar.sh                      # Compilacion OK -> out/
./probar.sh                        # 201 pruebas: 117 + 44 + 40, todas OK
./ejecutar_programas.sh            # genera salidas/*_captura.txt
./ventana.sh                       # abre la ventana grafica
java -cp out ui.Emulador factorial --traza --estado   # traza + estado
```

Estructura final (5 paquetes grandes + clientes):

- `src/cpu` — `CPU` (ciclo de 5 etapas IF/ID/EX/MEM/WB) y `PC`.
- `src/ejecucion` — `EjecutorInstrucciones`, `UnidadControl`, `SenalesControl`.
- `src/memoria` — `MemoriaSistema`, `UnidadAlmacenamiento`, `ROM` ("texto"@0),
  `RAM` ("datos"@100), `DiscoDuro` ("disco"@200), `Registros`.
- `src/alu` — `ALU` y `OperacionALU` (incluye LUI).
- `src/instruccion` — `Instruccion`, `Decodificador`, `Opcode` (HALT=0x3F),
  `Funct`, `FormatoInstruccion`, `ServicioSyscall`, `ProgramaBuilder`,
  `Desensamblador`, `Binario`.
- `src/ui`, `src/programas`, `src/pruebas` — clientes del núcleo.

Decisiones que conviene mencionar en el reporte:

- **HALT propio** (opcode 0x3F, "alto"): MIPS real no lo tiene; con él la CPU
  sabe que el programa terminó sin "morirse".
- **CPU nunca muere**: arranca en 0, el PC es circular, la celda 0 contiene un
  NOP (0x00000000) y hay límite de 100000 ciclos por seguridad.
- **Segmentos con dirección**: cada unidad tiene nombre + dirección base; el
  mapa muestra dirección decimal y hexadecimal.
- **RAM en 000**: verificado por pruebas y por las capturas.
- **Señales de control con Builder**: para no cometer errores de argumentos
  posicionales al crear señales.

---

## 7. Pendientes antes de entregar

- [x] Nombres y matrículas de la portada → sección 1.
- [x] Unificar el término **"emulador"** en código, scripts, documentación y
      diagramas (hecho: clases `ui.Emulador` y `ui.VentanaEmulador`,
      `./compilar.sh`, `./probar.sh` y diagramas regenerados).
- [x] Declaración de IA → rubro **"Uso de IA"** con los 13 prompts de
      `docs/reporte_exposicion/prompts_usados.md` (se excluyó el de `DiscoDuro`).
- [x] Diagramas de clases regenerados (`docs/diagramas/*.png`) con los nombres
      actuales.
- [x] **Reporte final redactado**: `docs/reporte_exposicion/reporte_final/reporte_final.tex`
      (copia modificada de la plantilla UV `Reporte_LaTeX/main.tex`; secciones:
      introducción, desarrollo, evidencia con 2 tablas y **12 figuras**, uso de
      IA con los 13 prompts y sus **34 capturas insertadas**, **reflexiones
      finales una por integrante** y referencias).
- [x] **Reporte compilado**: `reporte_final.pdf` (60 páginas, tamaño carta,
      compilado con XeLaTeX vía Tectonic; el `.docx` anterior se eliminó por no
      seguir el formato de la plantilla).
- [x] **Portada y carátula trasera unidas**: `reporte_final_completo.pdf`
      (62 páginas) = `portada.pdf` (pág. 1) + `reporte_final.pdf` (pág. 2–61) +
      `anteportada.pdf` (pág. 62, carátula trasera con el lema UV). Este es el
      **archivo final para subir a Eminus**.
- [x] **Capturas de pantalla (imágenes)**: `generar_capturas.py` convierte las
      salidas reales en 12 PNG de terminal dentro de `reporte_final/imagenes/`;
      las 34 capturas de los prompts están en `reporte_final/imagenes/ia/`.

## 8. Pendientes antes de subir a Eminus

- [ ] Revisar el PDF **completo** página por página (portada, índice,
      numeración de figuras y tablas, carátula trasera).
- [ ] Subir **únicamente** `reporte_final/reporte_final_completo.pdf` por
      Eminus.

## 9. Cómo regenerar los artefactos

```bash
./compilar.sh && ./probar.sh && ./ejecutar_programas.sh   # salidas reales
python3 docs/reporte_exposicion/reporte_final/generar_capturas.py      # 12 PNG
cd docs/reporte_exposicion/reporte_final
xelatex reporte_final.tex && xelatex reporte_final.tex   # índice completo
# o bien: tectonic reporte_final.tex   (compila dos pasadas solo)

# Unir portada + reporte + carátula trasera -> entregable final
cd ../..   # de nuevo en docs/reporte_exposicion
pdfunite portada.pdf reporte_final/reporte_final.pdf anteportada.pdf \
         reporte_final/reporte_final_completo.pdf
```

> **Orden del PDF final**: `portada.pdf` (pág. 1) + `reporte_final.pdf`
> (pág. 2–61) + `anteportada.pdf` (pág. 62, carátula trasera con el lema UV).
> En Windows/macOS sin `pdfunite`: unir con cualquier herramienta (p. ej.
> `pdftk`, o en línea) respetando ese orden.

> Las figuras de evidencia **no son inventadas**: `generar_capturas.py` toma el
> texto literal de `salidas/*_captura.txt` y de la salida de
> `pruebas.EjecutorPruebas` y sólo lo pinta con aspecto de terminal. Si cambian
> los programas, se vuelven a generar con los comandos de arriba.
> En Windows/macOS sin TeX: subir la carpeta `reporte_final/` completa a
> Overleaf y compilar con **XeLaTeX** (las imágenes están en `imagenes/`).
