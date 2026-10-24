# Guion de exposición — Práctica 2: Emulador del procesador MIPS en Java

> **Documento de apoyo, no es un guión para leer en voz alta.**
> Cada diapositiva de la presentación es apoyo visual: en pantalla van **pocas
> palabras**, la explicación la da el equipo. Aquí está, diapositiva por
> diapositiva: **qué idea transmitir**, **palabras clave** para no perderse y
> **dónde está eso en el código** por si el profesor pide verlo.
>
> Presentación: `docs/reporte_exposicion/exposicion/presentacion_exposicion.pptx` (35 diapositivas)
> Se regenera con: `python3 docs/reporte_exposicion/exposicion/generar_presentacion.py`
> Preguntas probables del profesor: `docs/reporte_exposicion/exposicion/preguntas_exposicion.md`
> Cómo hacer la demo en vivo (pasos, salida real y qué decir en pantalla):
> `docs/reporte_exposicion/exposicion/demo_ejecucion.md`

## Cómo se reparten los 45 minutos

| Minuto | Bloque | Diapositivas |
|--------|--------|--------------|
| 1 – 5 | Razones técnicas para elegir el microprocesador | 3 – 7 |
| 6 – 15 | Arquitectura (PC, ROM, RAM, memorias, registros) | 8 – 15 |
| 16 – 30 | ISA del procesador y pruebas técnicas | 16 – 23 |
| 30 – 45 | Códigos implementados y ejecutados | 24 – 35 |
| 45 – 55 | Preguntas del profesor (no hay diapositivas) | — |

Regla de oro: **no se lee la pantalla**. Si en una diapositiva hay 6 viñetas,
se dicen 3 en voz alta y las otras 3 quedan como referencia visual.

---

# Portada y contenido (antes del minuto 1)

## Diapositiva 1 — Portada
**Qué decir**
- Presentar la materia, el título de la práctica, al equipo (nombre y matrícula) y la fecha.
- Una frase de contexto: "es un emulador completo de un procesador MIPS escrito en Java, sin librerías externas".

**Palabras clave**: Microprocesadores y Microcontroladores · MIPS · Java · equipo · 23 de octubre de 2026.

**En el código**: nada que mostrar; solo tener a mano `./compilar.sh` por si arrancan la demo.

## Diapositiva 2 — Contenido
**Qué decir**
- Anunciar las 4 partes: razones técnicas, arquitectura, ISA + pruebas, códigos ejecutados.
- Decir que la última parte tiene demo en vivo.

**Palabras clave**: 4 bloques · 15 minutos de arquitectura · demo al final.

**En el código**: nada.

---

# Bloque 1 — Razones técnicas para elegir el microprocesador (min. 1 – 5)

## Diapositiva 3 — Separador "Razones técnicas para elegir el procesador"  *(1:00 – 1:20)*
**Qué decir**
- Frase de puente: antes de mostrar cómo está hecho, por qué elegimos MIPS y por qué emularlo en software.

**Palabras clave**: por qué MIPS · por qué un emulador.

**En el código**: nada.

## Diapositiva 4 — ¿Qué construimos?  *(1:20 – 2:00)*
**Qué decir**
- Un emulador de un procesador MIPS de 32 bits, escrito enteramente en Java y sin librerías externas.
- Tiene las piezas reales: contador de programa, registros, memorias, ALU, unidad de control y ciclo de instrucción completo.
- Cifras que impresionan y son verificables: 36 clases (22 en el núcleo), 5 programas de usuario, 201 pruebas automáticas.
- El objetivo didáctico: abrir la "caja negra" de un procesador.

**Palabras clave**: MIPS de 32 bits · Java puro · 36 clases · 5 paquetes · 5 etapas · 201 pruebas.

**En el código**: `src/` (36 archivos `.java`); núcleo en `src/cpu`, `src/ejecucion`, `src/memoria`, `src/alu`, `src/instruccion`.

## Diapositiva 5 — Procesador real vs. Emulador  *(2:00 – 2:40)*
**Qué decir**
- Un procesador real se diseña en silicio: cuesta caro, es propiedad de alguien y un error obliga a fabricar de nuevo.
- El emulador tiene los **mismos conceptos** (PC, registros, señales de control) pero todo está a la vista y se puede mirar ciclo a ciclo.
- Un "error" aquí es una prueba fallida que se corrige en minutos.

**Palabras clave**: silicio vs. software · interior visible · ciclo por ciclo · misma semántica.

**En el código**: `src/cpu/CPU.java:119` (`ciclo()`), `src/cpu/CPU.java:136-183` (las 5 etapas).

## Diapositiva 6 — MIPS frente a las alternativas  *(2:40 – 3:30)*
**Qué decir**
- Compara tres familias con 4 criterios: tipo de arquitectura, tamaño de instrucción, número de formatos, documentación.
- MIPS y RISC-V son RISC con instrucción fija de 32 bits; x86 es CISC con longitud variable (de 1 a 15 bytes) y decodificación mucho más complicada.
- Conclusión honesta: RISC-V también valía, MIPS ganó por documentación didáctica; x86 estaba fuera de alcance.

**Palabras clave**: RISC vs CISC · 32 bits fijos · 3 formatos vs 6 vs decenas · documentación pública.

**En el código**: `src/instruccion/Opcode.java` (16 códigos), `src/instruccion/Funct.java` (18 funct), `src/instruccion/Decodificador.java:45` (extraer opcode con una máscara).

## Diapositiva 7 — Tres razones técnicas  *(3:30 – 5:00)*
**Qué decir**
1. Es RISC: todas las instrucciones miden lo mismo → decodificar es barato.
2. Solo tres formatos fijos (R, I, J) → decodificar son máscaras y desplazamientos, nada de microcódigo.
3. Especificación abierta y docente: mismos conceptos que en cualquier CPU real (registros, memoria, ALU, pipeline).
- Cierre: "por eso pudimos implementar el ciclo completo sin perderse en el formato".

**Palabras clave**: longitud fija · R/I/J · máscaras + desplazamientos · sin microcódigo.

**En el código**: `src/instruccion/Decodificador.java:45-86` (opcode, rs, rt, rd, shamt, funct, inmediato, dirección J).

---

# Bloque 2 — Arquitectura del emulador (min. 6 – 15)

## Diapositiva 8 — Separador "Arquitectura del emulador"  *(6:00 – 6:20)*
**Qué decir**
- Frase de puente: ahora dentro del procesador: cómo está organizada la memoria, los registros y el ciclo.

**Palabras clave**: PC · ROM · RAM · registros · memorias de instrucciones y de datos.

**En el código**: nada.

## Diapositiva 9 — El sistema en 5 paquetes  *(6:20 – 7:30)*
**Qué decir**
- El núcleo son 5 paquetes: `cpu` (ciclo y estado), `ejecucion` (señales de control y ejecutor), `memoria` (ROM, RAM, disco, registros), `alu`, `instruccion` (decodificación y construcción de programas).
- Fuera del núcleo: interfaz gráfica, los 5 programas de usuario y las pruebas: **usan** el núcleo, no lo tocan.
- Esa separación es lo que permitió escribir 201 pruebas automáticas.

**Palabras clave**: 5 paquetes · núcleo vs. periferia · sin dependencias externas.

**En el código**: `src/cpu/`, `src/ejecucion/`, `src/memoria/`, `src/alu/`, `src/instruccion/`; fuera: `src/ui/`, `src/programas/`, `src/pruebas/`.

## Diapositiva 10 — Mapa de memoria  *(7:30 – 8:40)*
**Qué decir**
- Toda la memoria es un arreglo plano de **300 palabras de 32 bits** con direcciones 0 a 299; una dirección = una palabra.
- Tres segmentos: ROM 0–99 (texto/instrucciones), RAM 100–199 (datos), DiscoDuro 200–299 (persistencia).
- `MemoriaSistema` es el que sabe a qué segmento va cada dirección; si la dirección no existe, avisa con excepción.
- Las direcciones se pueden ver en decimal y en hexadecimal (dir 100 = 0x00000064).

**Palabras clave**: 300 celdas · una dirección = una palabra · 0–99 / 100–199 / 200–299 · `segmentoDe`.

**En el código**: `src/memoria/MemoriaSistema.java:35` (`segmentoDe`), `:62` (`leer`), `:67` (`escribir`), `:81` (`mapa()`); bases en `src/memoria/ROM.java:17`, `RAM.java:17`, `DiscoDuro.java:23`.

## Diapositiva 11 — Memoria de instrucciones (ROM) vs. Memoria de datos (RAM)  *(8:40 – 9:50)*
**Qué decir**
- **ROM = memoria de instrucciones**: ahí vive el programa; la CPU toma de ahí su instrucción en la etapa IF. Es de solo lectura: se carga y se bloquea.
- **RAM = memoria de datos**: vectores, cadenas y resultados; se toca solo con `lw` (leer) y `sw` (escribir), nunca directamente desde la aritmética.
- Esta es la separación **memoria de instrucciones / memoria de datos** que pide la práctica.
- El `DiscoDuro` (200–299) es opcional y guarda datos en archivo.

**Palabras clave**: texto vs. datos · solo lectura · `lw`/`sw` · `ROM.bloquear()`.

**En el código**: `src/memoria/ROM.java:35` (`cargarPrograma`), `:50` (`bloquear`), `:27` (escritura rechazada); `src/memoria/RAM.java:17` (base 100).

## Diapositiva 12 — Registros del procesador  *(9:50 – 11:00)*
**Qué decir**
- 32 registros de 32 bits **dentro de la CPU**: es la memoria más rápida, ahí vive la aritmética (ISA load/store).
- `$zero` siempre vale 0: cualquier escritura en el registro 0 se **ignora**. Esto es clave para el NOP (lo veremos en el bloque 3).
- `$v0` lleva el servicio del `syscall`, `$a0` el argumento, `$t*` temporales, `$s*` salvados, `$ra` retorno, `$sp` pila.
- Escrituras y lecturas pasan por una sola puerta: `Registros.escribir` / `Registros.leer`.

**Palabras clave**: 32 × 32 bits · `$zero` inmutable · load/store · `$v0`/`$a0`/`$ra`.

**En el código**: `src/memoria/Registros.java:29` (`leer`, devuelve 0 si índice 0), `:34` (`escribir`, descarta índice 0).

## Diapositiva 13 — PC: contador de programa  *(11:00 – 12:10)*
**Qué decir**
- El PC guarda la dirección de la **siguiente** instrucción y avanza en cada búsqueda.
- Aquí avanza **+1 celda** (no +4 bytes) porque una dirección = una palabra; en un MIPS real sería +4.
- Arranca en 0 (`PC.reiniciar()`) y es **circular**: si la dirección se sale del mapa, vuelve a 0 → la CPU nunca se muere por falta de código.
- Los saltos y ramas reescriben el PC: `beq`, `bne`, `j`, `jal`, `jr`.

**Palabras clave**: dirección siguiente · +1 celda (no +4) · arranca en 0 · circular · saltos reescriben el PC.

**En el código**: `src/cpu/PC.java:32` (`incrementar`), `:37` (`reiniciar`); `src/cpu/CPU.java:136-143` (IF y el reinicio circular), `:207` (`saltarA`).

## Diapositiva 14 — Ciclo de instrucción: 5 etapas  *(12:10 – 13:50)*
**Qué decir**
- Todo lo que hace un procesador es repetir este ciclo; aquí está dividido en 5 métodos que se llaman en orden:
  IF (traer la instrucción de la ROM y subir el PC) → ID (decodificar y pedir señales de control) → EX (la ALU calcula; ramas y saltos) → MEM (`lw`/`sw`) → WB (escribir el registro destino).
- Cada pasada suma 1 al contador de ciclos: así se miden los 63, 68, 123… de los programas.
- Aclaración honesta: es un ciclo **secuencial** (etapa por etapa), no un pipeline; el pipeline es trabajo futuro.

**Palabras clave**: IF → ID → EX → MEM → WB · `CPU.ciclo()` · contador de ciclos · secuencial, no pipeline.

**En el código**: `src/cpu/CPU.java:119` (`ciclo()`), `:128` (`ciclos++`), `:136`, `:146`, `:159`, `:164`, `:175` (las 5 etapas).

## Diapositiva 15 — Diagrama de clases del núcleo  *(13:50 – 15:00)*
**Qué decir**
- Muestra las 22 clases del núcleo y cómo se conectan: la CPU tiene ROM, RAM, registros, PC, ALU, memoria, unidad de control y ejecutor.
- Sirve para decir "el diseño es OO, pero cada clase modela una pieza física del procesador".
- Invitar a revisarlo en el repositorio (PNG en `docs/diagramas/`).

**Palabras clave**: 22 clases · 5 paquetes · composición · sin librerías externas.

**En el código**: `docs/diagramas/diagrama_clases_nucleo.png`; clases clave: `src/cpu/CPU.java:37-45` (las piezas que la CPU posee).

---

# Bloque 3 — ISA del procesador y pruebas técnicas (min. 16 – 30)

## Diapositiva 16 — Separador "ISA del procesador y pruebas técnicas"  *(16:00 – 16:20)*
**Qué decir**
- Frase de puente: de la estructura al "contrato": qué instrucciones acepta y cómo lo comprobamos.

**Palabras clave**: formatos · instrucciones · ALU · syscall · 201 pruebas.

**En el código**: nada.

## Diapositiva 17 — La ISA es el contrato del procesador  *(16:20 – 17:40)*
**Qué decir**
- La ISA es la interfaz entre software y hardware: define **instrucciones**, **registros** y **modos de direccionamiento**.
- Cadena: programa → compilador/ensamblador → ISA → microarquitectura → circuitos.
- MIPS publica su especificación y usa formatos fijos → la pudimos implementar completa.
- En el código, la ISA son tres enums y una clase de decodificación.

**Palabras clave**: ISA = contrato · instrucciones + registros + direccionamiento · software/hardware.

**En el código**: `src/instruccion/Opcode.java`, `src/instruccion/Funct.java`, `src/instruccion/FormatoInstruccion.java`, `src/instruccion/Decodificador.java`.

## Diapositiva 18 — Una instrucción = una palabra de 32 bits  *(17:40 – 19:20)*
**Qué decir**
- Los tres formatos, con el número de bits de cada campo: R = 6+5+5+5+5+6; I = 6+5+5+16; J = 6+26. Los tres suman 32.
- El opcode son los bits 31..26 → se extrae con `(palabra >>> 26) & 0x3F`.
- Ejemplo real: `addi $t0, $zero, 5` = `001000 00000 01000 0000000000000101`.
- Eso es todo lo que hace el decodificador: máscaras y desplazamientos.

**Palabras clave**: 32 bits · opcode bits 31..26 · campos R/I/J · `>>> 26 & 0x3F`.

**En el código**: `src/instruccion/Decodificador.java:45` (opcode), `:50-70` (rs/rt/rd/shamt/funct), `:75` (inmediato), `:85` (dirección J); `src/instruccion/Instruccion.java:72,81,88` (`codificarR/I/J`).

## Diapositiva 19 — ISA implementada: 33 instrucciones  *(19:20 – 21:00)*
**Qué decir**
- Conteo por formato: 18 de tipo R (opcode 0x00, la operación concreta la decide `funct`), 12 de tipo I, 2 de tipo J y 1 propia del emulador (`halt`, 0x3F).
- En R el opcode vale 0 y manda el campo `funct` de 6 bits: eso es lo que permite que haya 18 operaciones con el mismo opcode.
- Toda la lista está documentada en `docs/instrucciones.md`.

**Palabras clave**: 33 instrucciones · 18 R + 12 I + 2 J + halt · opcode 0 ≠ una sola operación · funct.

**En el código**: `src/instruccion/Opcode.java:13-28` (16 opcodes), `src/instruccion/Funct.java:11-28` (18 funct), `docs/instrucciones.md`.

## Diapositiva 20 — 0x00 (la operación 0) vs. 0x3F (HALT)  *(21:00 – 23:00)*
**Qué decir** — *esta es la diapositiva estrella: responde a "¿qué mantiene siempre trabajando a la CPU?" y "¿cómo se detiene?"*
- La palabra `0000…0000` se interpreta como opcode 0x00 (tipo R) + funct 0x00 (`sll`), es decir: "desplazar 0 y escribir el resultado en `$rd`". Como `$rd` = registro 0, la escritura se descarta → **no pasa nada**.
- Toda celda vacía de la memoria vale 0, así que la CPU siempre encuentra una instrucción inocua: **es un NOP gratis** y por eso nunca muere.
- La parada no es "que no haya código", es una instrucción explícita: `halt` (opcode 0x3F, propio del emulador) o `syscall` con `$v0 = 10`.
- Red de seguridad: si el programa no trae `halt`, se corta a los 100 000 ciclos.

**Palabras clave**: operación 0 = `sll` a `$zero` = NOP · celdas vacías valen 0 · `halt` 0x3F · syscall 10 · límite 100 000.

**En el código**: `src/instruccion/Instruccion.java:14` (`PALABRA_NOP = 0`); `src/memoria/Registros.java:34` (escrituras en 0 se ignoran); `src/ejecucion/UnidadControl.java:61-62` (HALT → señal `halt()`); `src/ejecucion/EjecutorInstrucciones.java:31-34` (señal → `cpu.detener()`); `src/cpu/CPU.java:35` (límite), `:330` (`detener()`).

## Diapositiva 21 — ALU  *(23:00 – 25:00)*
**Qué decir**
- Dos operandos de 32 bits + una operación → 32 bits de resultado. Familias: aritmética, lógica, comparación, desplazamiento.
- **MIPS no tiene banderas (flags)**: para comparar se usa `slt` y luego `beq`/`bne`.
- Desbordamiento: `add`/`sub` **detienen** la ejecución si el resultado no cabe en 32 bits; `addu`/`subu` envuelven sin quejarse (por eso los programas los usan).
- `mult`/`div` guardan 64 bits en el par **HI:LO**, que se recuperan con `mfhi`/`mflo`.

**Palabras clave**: 32→32 bits · sin flags → `slt` + `bne` · `add` truena, `addu` envuelve · HI:LO.

**En el código**: `src/alu/ALU.java:11` (`operar`), `:16` y `:25` (excepción de desbordamiento), `:20`/`:29` (addu/subu), `:39` (slt), `:47` (lui); `src/alu/OperacionALU.java`.

## Diapositiva 22 — syscall: la puerta al sistema operativo  *(25:00 – 27:00)*
**Qué decir**
- En un procesador real el `syscall` es la frontera entre modo usuario y sistema operativo (entrada/salida); aquí saca por la consola del emulador.
- El servicio va en `$v0` y los argumentos en `$a0`: 1 imprimir entero, 4 imprimir cadena, 5 leer entero, 11 imprimir carácter, **10 terminar**.
- Servicio 10 es la segunda forma de parar la CPU (junto con `halt`).

**Palabras clave**: `$v0` = servicio · `$a0` = argumento · 1/4/5/11/10 · frontera programa–SO.

**En el código**: `src/cpu/CPU.java:218` (`syscall()`), `:219` (lee `$v0`), `:253-255` (servicio 10 → `detenido = true`); enum `src/instruccion/ServicioSyscall.java`.

## Diapositiva 23 — Pruebas técnicas: 201, todas pasan  *(27:00 – 30:00)*
**Qué decir**
- Tres niveles: **117 unitarias** (ALU, registros, decodificador, segmentos, HALT), **44 de integración** (ciclo completo, ramas, saltos, syscall), **40 de sistema** (los 5 programas con su salida exacta).
- Verifican también propiedades de arquitectura: que la CPU nunca muere, que la ROM queda bloqueada, que `$zero` no cambia.
- Una sola orden: `./probar.sh` → 201 pruebas, 0 fallidas (el resumen se guarda en `salidas/plan_pruebas_completo.txt`).
- Mostrar la captura de la terminal como evidencia.

**Palabras clave**: 117 + 44 + 40 = 201 · 0 fallidas · `./probar.sh` · 3 niveles de prueba.

**En el código**: `src/pruebas/PruebasUnitarias.java`, `PruebasIntegracion.java`, `PruebasSistema.java`, corren con `src/pruebas/EjecutorPruebas.java` y `probar.sh`; planes en `docs/pruebas/plan_pruebas_*.md`.

---

# Bloque 4 — Códigos implementados y ejecutados (min. 30 – 45)

## Diapositiva 24 — Separador "Códigos implementados y ejecutados"  *(30:00 – 30:20)*
**Qué decir**
- Frase de puente: ya se vio la teoría; ahora lo que corre de verdad, con salidas y ciclos medidos.

**Palabras clave**: 5 programas · evidencia de ejecución.

**En el código**: nada.

## Diapositiva 25 — Los 5 programas de usuario  *(30:20 – 31:30)*
**Qué decir**
- Tabla maestra: qué hace cada programa, su salida real y sus ciclos (52/68, 5040/63, 30/56, 13/123, z=3/y=1/62).
- Todos se construyen con `ProgramaBuilder` (sin escribir binario a mano) y **terminan con `halt()`**.
- Los ciclos son la medida objetiva de complejidad: el promedio es el más largo porque hace dos bucles.

**Palabras clave**: sumatoria · factorial · integral · promedio · perceptron · ciclos por programa.

**En el código**: `src/programas/*.java` (5 programas + `Impresion.java`); se arman con `src/instruccion/ProgramaBuilder.java`.

## Diapositiva 26 — Factorial  *(31:30 – 32:30)*
**Qué decir**
- Calcula 7! = 5040 en 63 ciclos; compara sin flags (`slt` + `bne`) y multiplica con `mult`/`mflo`.
- El acumulado vive en un registro y pasa por LO en cada multiplicación.
- Las etiquetas (`"bucle"`, `"fin_bucle"`) las resuelve `ProgramaBuilder` al generar el binario.

**Palabras clave**: 7! = 5040 · `slt`+`bne` · `mult`/`mflo` · etiquetas · `Impresion.terminar` → halt.

**En el código**: `src/programas/Factorial.java`; etiquetas en `src/instruccion/ProgramaBuilder.java:131`, resolución en `generar()`.

## Diapositiva 27 — Sumatoria  *(32:30 – 33:20)*
**Qué decir**
- Suma de 3 a 10 → 52 en 68 ciclos.
- Los datos están en la **RAM** y se leen con `lw` (dirección absoluta con base `$zero`): esto demuestra el flujo datos-en-RAM / código-en-ROM.
- Usa `addu`/`addiu` para que nunca pueda detenerse por desbordamiento; el bucle sale cuando `slt` detecta que `i` pasó el final.

**Palabras clave**: 3..10 = 52 · datos en RAM · `lw` con base `$zero` · `addu` sin desbordamiento.

**En el código**: `src/programas/Sumatoria.java`; `src/instruccion/ProgramaBuilder.java:263` (`lwAbs`), `:92` (`palabra` devuelve la dirección en RAM).

## Diapositiva 28 — Integral  *(33:20 – 34:10)*
**Qué decir**
- Suma de cuadrados 1²+2²+3²+4² = 30 en 56 ciclos, el más corto.
- Calcula x·x con `mult`+`mflo`, recorre x con `addiu` y acumula en un registro.
- Ejemplo de que un cálculo "numérico" completo cabe en 20 instrucciones.

**Palabras clave**: Σx² = 30 · 56 ciclos · `mult`+`mflo` · acumulador en registro.

**En el código**: `src/programas/Integral.java`.

## Diapositiva 29 — Promedio  *(34:10 – 35:10)*
**Qué decir**
- Promedio entero de {4,8,15,16,23} = 13 (66/5) en 123 ciclos, el más largo.
- El vector vive en la RAM y se recorre con direccionamiento **base + índice**: `addu T6, T4, T1` y luego `lw (T6)`.
- Dos bucles (imprimir y sumar) y la división entera con `div`+`mflo`.

**Palabras clave**: vector en RAM · base + índice · `div`/`mflo` · 123 ciclos = dos bucles.

**En el código**: `src/programas/Promedio.java`; `ProgramaBuilder.celda()` en `:126`; `lw` en `:254`.

## Diapositiva 30 — Perceptron  *(35:10 – 36:00)*
**Qué decir**
- Red neuronal mínima: z = Σ x·w + bias = 3 → activación y = 1, en 62 ciclos.
- Entradas, pesos y bias están guardados en la RAM como datos; el producto se hace con `mult`/`mflo`.
- La activación escalonada es un `slt` + `bne`: si z < 0, y se queda en 0.

**Palabras clave**: z = 3 → y = 1 · pesos en RAM · `mult`+`mflo` · escalón con `slt`.

**En el código**: `src/programas/Perceptron.java`.

## Diapositiva 31 — Evidencia de ejecución  *(36:00 – 37:00)*
**Qué decir**
- Mostrar la captura de la terminal: salida del programa + "FIN DE EJECUCION … ciclos: 63", PC final, LO = 5040 y el volcado de la RAM.
- Repetir la orden para que quede grabada: `java -cp out ui.Emulador factorial --traza --estado`.

**Palabras clave**: salida real · ciclos · `--estado` · PC/LO/RAM al terminar.

**En el código**: `src/ui/Emulador.java:43-52` (flags), `:114-124` (bucle y traza), imagen en `docs/reporte_exposicion/reporte_final/imagenes/ev_factorial_fin.png`.

## Diapositiva 32 — Demo en vivo  *(37:00 – 40:30)*
**Qué decir** (reserva 3 minutos; preparar los comandos con anticipación)
1. `./compilar.sh` — compila sin librerías externas.
2. `java -cp out ui.Emulador factorial --traza --estado` — se ven los ciclos y el estado final.
3. `./probar.sh` — las 201 pruebas en vivo (si hay prisa, solo el resumen final).
4. `./ventana.sh` — ventana gráfica: "Ejecutar 1 ciclo" muestra la instrucción, sus señales de control y el PC siguiente (`Ctrl+Intro`).
- Si algo falla, tienen la captura de respaldo en `salidas/*_captura.txt`.

**Palabras clave**: `compilar.sh` · `--traza` · `--estado` · `probar.sh` · `ventana.sh` · un ciclo a la vez.

**En el código**: `src/ui/Emulador.java` (CLI), `src/ui/VentanaEmulador.java` (ventana), guiones `compilar.sh`, `probar.sh`, `ejecutar_programas.sh`, `ventana.sh`.

## Diapositiva 33 — Conclusiones  *(40:30 – 42:00)*
**Qué decir**
- MIPS fue la elección correcta: ISA reducida, 3 formatos fijos, documentación clara.
- El diseño es modular: agregar una instrucción toca 4 archivos y nada más (Opcode → Decodificador → UnidadControl → Ejecutor, más la prueba).
- La CPU nunca muere: arranca en 0, las celdas vacías son NOP y solo `halt` o syscall 10 la paran.
- La evidencia es verificable: 201 pruebas y 5 programas con salida exacta.

**Palabras clave**: elección acertada · modularidad · CPU nunca muere · evidencia.

**En el código**: `src/instruccion/Opcode.java` → `Decodificador.java` → `src/ejecucion/UnidadControl.java` → `src/ejecucion/EjecutorInstrucciones.java` (la cadena de 4 archivos).

## Diapositiva 34 — Trabajo futuro  *(42:00 – 43:30)*
**Qué decir**
- Pipeline real (los 5 en paralelo) con sus riesgos de datos y de control.
- Más instrucciones (`lb`/`sb`, `lui` completo), excepciones reales para el desbordamiento, caché y ensamblador de texto `.s`.
- Responder aquí si preguntan "¿qué le falta?".

**Palabras clave**: pipeline · más instrucciones · excepciones · caché · ensamblador.

**En el código**: nada nuevo; mencionar `src/cpu/CPU.java:119` (hoy las etapas se llaman en secuencia).

## Diapositiva 35 — Gracias / ¿Preguntas?  *(43:30 – 44:00)*
**Qué decir**
- Cerrar con una frase corta y abrir el turno de preguntas.
- Tener a mano `preguntas_exposicion.md` (dentro del repositorio) para localizar cualquier línea de código.

**Palabras clave**: preguntas.

**En el código**: `docs/reporte_exposicion/exposicion/preguntas_exposicion.md`.

---

## Reparto sugerido del equipo

| Persona | Bloque | Diapositivas |
|---------|--------|--------------|
| A | Razones técnicas + ISA | 3 – 7 y 16 – 20 |
| B | Arquitectura | 8 – 15 |
| C | Pruebas + códigos + demo | 21 – 23 y 24 – 35 |

Cada quien responde las preguntas de su bloque; si alguien no sabe, se pasa la
pregunta en lugar de improvisar una respuesta.
