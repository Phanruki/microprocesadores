# Registro de información para el reporte final

> Este documento **no es el reporte final**: es todo lo acumulado para
> redactarlo después (donde sea necesario copiar/pegar y editar). El reporte
> final se entrega como **PDF por Eminus** (nunca por otro medio) usando la
> **plantilla UV**.

Requisitos del reporte (de `docs/reporte_final.txt`):

1. Portada: nombres, matrículas, título, fecha de entrega + plantilla UV.
2. Introducción: (a) razones para desarrollar un procesador propio desde cero,
   (b) ganancia de un alumno que sabe implementar un simulador de un procesador
   real, (c) por qué se implementó un emulador para el procesador MIPS.
3. Evidencia de ejecución: tabla "Línea de código a ejecutar | resultado del
   emulador".
4. Uso de IA: incluir los PROMPTs.
5. Reflexiones finales: nivel de programación requerido, trabajo en equipo, y
   qué le conviene a los que cursan el año próximo.
6. Axiológico (libertad, responsabilidad, solidaridad, respeto, honestidad,
   integridad) e heurístico (cumplir las instrucciones de la práctica).

---

## 1. Datos de la portada (rellenar)

- Título: "Emulador básico de un procesador (Práctica 2)" — *o el que elija el
  equipo*.
- Materia / universidad: Universidad Veracruzana (usar la plantilla UV).
- Nombres y matrículas: **pendiente de completar por el equipo**.
- Fecha de entrega: **pendiente**.

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

### 2.2 ¿Qué gana el alumno al saber implementar un simulador de un procesador real?

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
| `java -cp out ui.Simulador sumatoria --estado --captura salidas/sumatoria_captura.txt` | `Sumatoria de 3 a 10 = 52` y `FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 68` |
| `java -cp out ui.Simulador factorial` | `Factorial de 7 = 5040` (ciclos: 63) |
| `java -cp out ui.Simulador integral` | `Integral de x^2 dx desde x=1 hasta x=5 = 30` (ciclos: 56) |
| `java -cp out ui.Simulador promedio` | `Vector: 4 8 15 16 23` / `Promedio = 13` (ciclos: 123) |
| `java -cp out ui.Simulador perceptron` | `z = 3  y = 1` (ciclos: 62) |
| `java -cp out pruebas.PruebasUnitarias` | `RESUMEN PRUEBAS UNITARIAS / Pasadas: 117  Fallidas: 0` |
| `java -cp out pruebas.PruebasIntegracion` | `RESUMEN PRUEBAS DE INTEGRACION / Pasadas: 44  Fallidas: 0` |
| `java -cp out pruebas.PruebasSistema` | `RESUMEN PRUEBAS DE SISTEMA / Pasadas: 40  Fallidas: 0` |
| `java -cp out pruebas.EjecutorPruebas` | `Resultado global: todas las pruebas pasaron` (201 pruebas) |
| `java -cp out ui.Simulador factorial --traza --estado` | traza ciclo a ciclo (PC, binario y ensamblador) + volcado de registros y mapa de memoria |

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

### 3.4 Evidencia de la ventana gráfica (`ui.VentanaSimulador`)

- Historial por ciclo: `Ciclo`, `PC`, instrucción en binario + ensamblador,
  operación, **señales de control** y `PC Siguiente`.
- Paneles de Registros, Memoria (por segmentos) y Estado CPU (PC, HI, LO).

---

## 4. Uso de IA: PROMPTs (declarar plenamente; el rubro lo exige)

La práctica pide declarar el uso de IA con sus prompts. Registrar aquí los
prompts usados; el equipo debe completar los suyos (historial de la
conversación). Ejemplo de prompts usados en este proyecto:

1. "Analiza las siguientes clases. <CÓDIGO>" *(prompt de la práctica, Paso 3)*.
2. "Genera un plan de pruebas para el emulador del procesador MIPS."
3. "Genera un plan de pruebas de integración de código para el emulador de
   procesador MIPS."
4. "Genera un plan de pruebas de sistema para el código para el emulador del
   procesador MIPS."
5. "Genera un código en lenguaje ensamblador para el procesador MIPS que
   implemente el factorial de un número, con explicación de cada línea, usando
   solo estas instrucciones: <lista>".
6. *(Este proyecto)* "Reorganiza el emulador en 5 paquetes grandes (cpu,
   ejecucion, memoria, alu, instruccion) al estilo del diagrama del compañero,
   con la memoria segmentada por dirección (ROM texto@0, RAM datos@100,
   disco@200), una CPU que nunca muere (arranca en 0, PC circular, memoria en
   0 = NOP, límite de 100000 ciclos) y una instrucción HALT para que la CPU sepa
   que el programa terminó; añade pruebas y regenera los diagramas."

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
java -cp out ui.Simulador factorial --traza --estado   # traza + estado
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

- [ ] Completar nombres, matrículas y fecha en la portada (plantilla UV).
- [ ] Redactar la introducción con el material de la sección 2 (en voz propia).
- [ ] Elegir 5–8 filas de las tablas de la sección 3 (copiar la salida real).
- [ ] Pegar los prompts reales del equipo en la sección 4.
- [ ] Redactar las reflexiones (sección 5) con voz propia del equipo.
- [ ] Exportar a **PDF** y subirlo **únicamente por Eminus**.
