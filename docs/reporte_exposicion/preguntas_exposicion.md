# Preguntas probables del profesor — Práctica 2 (Emulador MIPS en Java)

> **Cómo usar este documento si no conoces el código:**
> cada pregunta trae la respuesta corta y, debajo, **`archivo:línea`**.
> Abre el archivo en el editor y ve a esa línea; ahí está la prueba de lo que
> dices. No hace falta entender Java para leer la línea: casi todas son
> comentarios o una instrucción de una sola línea.
>
> Al final hay un **mapa rápido del código** y recetas para encontrar cosas
> con una sola orden de terminal.

---

## Las tres preguntas clave (las que ya se repitieron)

### P1. ¿Cómo guarda la memoria los datos? ¿Cómo funcionan las direcciones y los "punteros"?

**R.** No hay punteros como en C: hay **direcciones de memoria**. Toda la memoria
es un arreglo plano de **300 palabras de 32 bits**, con direcciones 0 a 299.
Una dirección = una palabra (no un byte). El mapa es:

| Segmento | Direcciones | Qué guarda |
|----------|-------------|------------|
| ROM (`texto`) | 0 – 99 | las instrucciones del programa |
| RAM (`datos`) | 100 – 199 | datos: vectores, cadenas, resultados |
| DiscoDuro (`disco`) | 200 – 299 | persistencia en archivo |

Una variable "es" una celda de la RAM. El programa guarda en un **registro**
la dirección de esa celda y la usa con `lw` (leer) o `sw` (escribir). Ejemplo:
`p.palabra(3)` reserva una celda en la RAM y **devuelve su dirección** (100, 101…);
esa dirección es lo más parecido a un "puntero" que existe aquí.

- Dirección = base + índice: `ProgramaBuilder.celda(base, i)` simplemente suma.
- Forma absoluta: `lwAbs(rt, dir)` = `lw` con base `$zero` → `dir + 0`.
- Forma con desplazamiento: en la ejecución se calcula `base + inmediato`.
- Si la dirección no existe (fuera de 0–299), `MemoriaSistema` lanza
  `IndexOutOfBoundsException` en lugar de escribir en cualquier parte.

**Dónde está el código**
- `src/memoria/MemoriaSistema.java:35` — `segmentoDe(direccion)`: decide a qué segmento va cada dirección.
- `src/memoria/MemoriaSistema.java:62` y `:67` — `leer` / `escribir` usando esa decisión.
- `src/memoria/MemoriaSistema.java:72-76` — error si la dirección está fuera del mapa.
- `src/memoria/ROM.java:17`, `src/memoria/RAM.java:17`, `src/memoria/DiscoDuro.java:23` — direcciones base 0, 100 y 200.
- `src/memoria/UnidadAlmacenamiento.java:53-66` — conversión dirección absoluta ↔ local.
- `src/instruccion/ProgramaBuilder.java:92` — `palabra(valor)` reserva una celda y **devuelve su dirección** (el "puntero").
- `src/instruccion/ProgramaBuilder.java:99` y `:110` — `cadena()` y `reservar(n)` hacen lo mismo con bloques.
- `src/instruccion/ProgramaBuilder.java:254` y `:263` — `lw` y `lwAbs`.
- `src/ejecucion/EjecutorInstrucciones.java:88-96` — la dirección efectiva `a + inmediato` que usa `lw`/`sw`.
- `src/ui/Emulador.java:94-96` — así se cargan los datos en la RAM al arrancar.

---

### P2. ¿Tiene operación de parada? ¿Cómo sabe la CPU que se detuvo?

**R.** Sí. La CPU se detiene por **una de tres razones**, y en ningún otro caso:

1. **`halt`** — instrucción propia del emulador con opcode `0x3F`. Su bit de
   señal `esHalt` hace que el ejecutor llame a `cpu.detener()`, que pone
   `detenido = true`.
2. **`syscall` con `$v0 = 10`** (servicio "terminar programa") — también pone
   `detenido = true`.
3. **Límite de seguridad**: 100 000 ciclos. Si el programa no trae ninguna de
   las anteriores, el bucle principal se corta solo y reporta que no terminó.

Cómo "sabe" la CPU: el campo `detenido` es un flag de la clase `CPU`. Mientras
sea `false`, `CPU.ciclo()` hace las 5 etapas; en cuanto es `true`, `ciclo()`
regresa sin hacer nada y los bucles (`ejecutar()`, la CLI, la ventana) salen.
Al terminar se puede consultar con `cpu.isDetenido()`.

**Dónde está el código**
- `src/cpu/CPU.java:330-332` — `detener()` pone `detenido = true`.
- `src/cpu/CPU.java:325-327` — `isDetenido()`.
- `src/cpu/CPU.java:119-121` — `ciclo()` hace `return` inmediato si está detenida.
- `src/cpu/CPU.java:35` — `LIMITE_CICLOS_SEGURIDAD = 100000`.
- `src/cpu/CPU.java:273-276` — `ejecutar()`: `while (!detenido && ciclos < LIMITE…)`; devuelve `true` si paró por HALT.
- `src/cpu/CPU.java:253-255` — caso `TERMINAR` del syscall → `detenido = true`.
- `src/instruccion/Opcode.java:28` — `HALT("halt", 0x3F, …)`.
- `src/instruccion/Instruccion.java:93` — `codificarHalt()`.
- `src/ejecucion/UnidadControl.java:61-62` — `case HALT` → señal `halt()`.
- `src/ejecucion/SenalesControl.java:55` y `:207` — la señal `esHalt`.
- `src/ejecucion/EjecutorInstrucciones.java:31-34` — `if (senales.esHalt) { cpu.detener(); return; }`.
- `src/ui/Emulador.java:130-136` — el mensaje "FIN DE EJECUCION … ciclos" (o el aviso del límite de seguridad).

---

### P3. ¿Tiene "operación 0"? ¿Qué impide que la CPU se detenga y qué la mantiene siempre trabajando?

**R.** Sí, y ese es justamente el truco. La palabra `00000000 00000000 00000000 00000000`
se decodifica como **opcode `0x00` (formato R) + funct `0x00` (`sll`)**, es decir
"desplazar a la izquierda 0 posiciones" y escribir el resultado en el registro
`rd`. Como los 5 bits de `rd` valen 0, el destino es **`$zero`**, y las
escrituras en el registro 0 se **ignoran**: no ocurre nada. Eso es un **NOP**.

¿Qué mantiene viva a la CPU?

1. **Toda celda vacía de la memoria vale 0**, así que aunque el PC salga del
   código siempre "lee" un NOP: la CPU encuentra trabajo inocuo y sigue.
2. **El PC es circular**: si la dirección queda fuera del mapa (0–299), en la
   etapa IF vuelve a `ROM.DIRECCION_BASE` (0) en vez de morir.
3. **No hay fallo por "código inexistente"**: solo `halt`/syscall 10 la paran.
   El límite de 100 000 ciclos no es una parada normal: es una red de seguridad
   para que un programa sin `halt` no se cuelgue para siempre.

Nota de precisión: en un MIPS real la palabra 0 es `sll $0,$0,0` y se usa como
NOP; aquí funciona igual porque la arquitectura define que escribir en `$zero`
se descarta.

**Dónde está el código**
- `src/instruccion/Instruccion.java:14` — `PALABRA_NOP = 0`.
- `src/instruccion/Opcode.java:13` — opcode `0x00` = tipo R.
- `src/instruccion/Funct.java:11` — funct `0x00` = `sll`.
- `src/instruccion/Decodificador.java:15-32` — cómo separa opcode, funct y campos.
- `src/ejecucion/UnidadControl.java:96-97` — `case SLL` → `alu(SLL).escribeRegistro()`.
- `src/ejecucion/EjecutorInstrucciones.java:104-106` — el registro destino en R es `rd` (= 0 en la palabra 0).
- `src/memoria/Registros.java:34-39` — **`if (indice != 0)`**: la escritura en `$zero` se descarta.
- `src/cpu/CPU.java:136-143` — etapa IF con el reinicio circular del PC.
- `src/cpu/CPU.java:35` y `:273-276` — límite de 100 000 ciclos.

---

## A. Arquitectura y memoria

### P4. ¿Qué es el PC y cómo sabe cuál es la siguiente instrucción?
**R.** El PC (contador de programa) guarda la dirección de la **siguiente**
instrucción. En cada etapa IF se lee la instrucción de esa dirección y luego el
PC avanza **+1 celda** (aquí una dirección = una palabra; en un MIPS real serían
+4 bytes). Los saltos y ramas sobrescriben el PC con `set()`.

**Código**: `src/cpu/PC.java:23-34` (`get`, `set`, `incrementar`), `:37-39`
(`reiniciar`, arranca en 0); `src/cpu/CPU.java:136-143` (IF: leer y apuntar a
la siguiente); `src/cpu/CPU.java:207` (`saltarA`).

### P5. ¿Por qué el PC avanza de 1 en 1 y no de 4 en 4?
**R.** Porque la memoria está modelada como un arreglo de **palabras** de 32
bits, no de bytes: la dirección 5 es la instrucción número 5. Sumar 4 saltaría
3 instrucciones. Es una simplificación didáctica declarada en la práctica; la
semántica de la arquitectura es la misma.

**Código**: `src/cpu/PC.java:30-34` (comentario: "incrementa el PC en una
palabra"); `src/cpu/CPU.java:142` (`pc.set(direccion + 1)`).

### P6. ¿Qué diferencia hay entre memoria de instrucciones y memoria de datos?
**R.** Están separadas: la **ROM (0–99)** guarda el programa y es de **solo
lectura** (se carga y se bloquea); la **RAM (100–199)** guarda los datos y solo
se toca con `lw`/`sw`. La CPU toma sus instrucciones de la ROM en IF, y los
datos entran y salen de la RAM en MEM.

**Código**: `src/memoria/ROM.java:17` (base 0), `:35` (`cargarPrograma`),
`:27-33` (escritura rechazada si está bloqueada), `:50` (`bloquear`);
`src/memoria/RAM.java:17` (base 100); `src/cpu/CPU.java:141` (IF lee la ROM),
`:164-173` (MEM lee/escribe datos).

### P7. ¿Qué pasa si el programa intenta leer o escribir una dirección inexistente?
**R.** `MemoriaSistema` lanza `IndexOutOfBoundsException("Direccion fuera del
mapa…")`: el error se reporta en lugar de corromper otra celda. Y si se intenta
escribir en la ROM bloqueada, lanza `IllegalStateException("ROM bloqueada…")`.

**Código**: `src/memoria/MemoriaSistema.java:72-76`; `src/memoria/ROM.java:27-33`.

### P8. ¿Cómo se ve el mapa completo de memoria?
**R.** Con `MemoriaSistema.mapa()`, que lista cada segmento con sus direcciones
en decimal y hexadecimal. Se imprime con `--estado` en la CLI o desde la ventana.

**Código**: `src/memoria/MemoriaSistema.java:81-91`; ejemplo de uso
`src/ui/Emulador.java:105` y 141.

### P9. ¿Qué hace el `DiscoDuro` si la práctica solo pide RAM y ROM?
**R.** Es un tercer segmento (200–299) que demuestra que el mapa es extensible:
guarda y carga su contenido en un archivo (`guardar`/`cargar`), o se importa y
exporta desde otra unidad. No interfiere con los dos segmentos pedidos.

**Código**: `src/memoria/DiscoDuro.java:23` (base 200), `:30` (`guardar`),
`:40` (`cargar`), `:56`/`:65` (importar/exportar).

### P10. ¿Qué son los registros y por qué 32?
**R.** Son la memoria más rápida, dentro de la CPU: 32 registros de 32 bits. El
ISA de MIPS define 32 registros con usos convencionales (`$zero`, `$v0`, `$a0`,
`$t*`, `$s*`, `$ra`, `$sp`…). Es la razón de que la aritmética sea "load/store":
solo los registros participan en operaciones; la memoria se toca con `lw`/`sw`.

**Código**: `src/memoria/Registros.java:9` (cantidad), `:29-32` (`leer`),
`:34-39` (`escribir`); nombres en `src/instruccion/ProgramaBuilder.java:24-55`.

### P11. ¿Qué le pasa al registro `$zero` si el programa escribe en él?
**R.** Nada: `escribir` tiene un `if (indice != 0)` y descarta la escritura;
`leer(0)` devuelve siempre 0. Es lo que hace que la palabra 0 sea un NOP.

**Código**: `src/memoria/Registros.java:34-39` y `:29-32`.

---

## B. Ciclo de instrucción y etapas

### P12. ¿Cuáles son las 5 etapas y dónde están en el código?
**R.** IF (buscar), ID (decodificar), EX (ejecutar), MEM (memoria), WB
(escribir). En `CPU.ciclo()` se llaman en ese orden y se suma 1 al contador de
ciclos.

**Código**: `src/cpu/CPU.java:119-130` (`ciclo()` y `ciclos++`), `:136` (IF),
`:146` (ID), `:159` (EX), `:164` (MEM), `:175` (WB).

### P13. ¿Es un pipeline? ¿Por qué las etapas no corren en paralelo?
**R.** No: es un ciclo **secuencial**; una etapa termina antes de empezar la
siguiente. Está dicho en el diseño y es el primer punto de trabajo futuro
("pipeline: las 5 etapas en paralelo y sus riesgos").

**Código**: `src/cpu/CPU.java:119-130` (llamadas en línea, sin solapamiento);
mencionar `docs/reporte_exposicion/presentacion_exposicion.pptx` diapositiva 34.

### P14. ¿Qué es exactamente un "ciclo" aquí?
**R.** Una pasada completa de las 5 etapas, es decir, la ejecución de **una
instrucción**. El contador `ciclos` aumenta de 1 en 1 por instrucción, por eso
"factorial = 63 ciclos" significa que ejecutó 63 instrucciones.

**Código**: `src/cpu/CPU.java:128` (`ciclos++`), `:334` (`getCiclos()`).

### P15. ¿Qué son las señales de control?
**R.** El resultado de decodificar: un conjunto de banderas (`escribeRegistro`,
`leeMemoria`, `escribeMemoria`, `esRama`, `esSalto`, `esHalt`, `esSyscall`,
`operacionAlu`…) que la etapa EX interpreta. Es el mismo concepto que en un
procesador real: la unidad de control traduce instrucción → señales.

**Código**: `src/ejecucion/SenalesControl.java:16-56` (los campos),
`src/ejecucion/UnidadControl.java:22-113` (cómo se generan),
`src/ejecucion/EjecutorInstrucciones.java:29-101` (cómo se aplican).

### P16. ¿Cómo se resuelven las ramas y los saltos?
**R.** En EX: `beq`/`bne` comparan dos registros y, si se cumple, calculan
`PC + inmediato`; `j`/`jal` usan la dirección de 26 bits; `jr` salta al valor de
un registro. `jal` además guarda la dirección de retorno en `$ra`.

**Código**: `src/ejecucion/EjecutorInstrucciones.java:69-74` (ramas),
`:76-83` (`j`/`jal` y `$ra`), `:84-87` (`jr`); `src/cpu/CPU.java:207` (`saltarA`).

---

## C. ISA: formatos, instrucciones y decodificación

### P17. ¿Cuántas instrucciones implementa y cómo se agrupan?
**R.** **33 en total**: 18 de formato R (opcode `0x00`, la operación la decide
el campo `funct`), 12 de formato I, 2 de formato J (`j`, `jal`) y 1 propia del
emulador (`halt`, `0x3F`).

**Código**: `src/instruccion/Opcode.java:13-28` (16 opcodes), 
`src/instruccion/Funct.java:11-28` (18 funct); lista completa y documentada en
`docs/instrucciones.md`.

### P18. ¿Cómo se decodifica una instrucción? ¿Dónde está eso?
**R.** Con máscaras y desplazamientos de bits. El opcode son los bits 31..26:
`(palabra >>> 26) & 0x3F`. Los demás campos salen igual (rs, rt, rd, shamt,
funct, inmediato de 16 bits, dirección de 26 bits).

**Código**: `src/instruccion/Decodificador.java:45` (opcode), `:50` (rs),
`:55` (rt), `:60` (rd), `:65` (shamt), `:70` (funct), `:75` (inmediato),
`:80` (extensión de signo), `:85` (dirección J), `:15-32` (ensambla todo).

### P19. ¿Cuáles son los tres formatos y por qué 32 bits?
**R.** R = opcode(6) + rs(5) + rt(5) + rd(5) + shamt(5) + funct(6); I =
opcode(6) + rs(5) + rt(5) + inmediato(16); J = opcode(6) + dirección(26).
Todos suman 32. Es la propiedad RISC de longitud fija.

**Código**: `src/instruccion/FormatoInstruccion.java`;
`src/instruccion/Instruccion.java:72` (`codificarR`), `:81` (`codificarI`),
`:88` (`codificarJ`); diagrama en la diapositiva 18.

### P20. Si el opcode es 0x00 para 18 instrucciones, ¿cómo se distinguen?
**R.** Por el campo `funct` (6 bits menos significativos): `add`=0x20,
`sub`=0x22, `sll`=0x00, `jr`=0x08, `syscall`=0x0C, etc. El decodificador busca
`Opcode` por el opcode y, si es tipo R, busca `Funct` por el funct.

**Código**: `src/instruccion/Decodificador.java:19` (`Funct.porCodigo(funct())`),
`src/instruccion/Funct.java:11-28`, `src/ejecucion/UnidadControl.java:70-113`
(`senalesTipoR`).

### P21. ¿Por qué `halt` no existe en el MIPS real?
**R.** Porque en un sistema real no se necesitaba: el sistema operativo
termina el proceso. Aquí el emulador necesita una forma explícita de parar la
CPU, así que se agregó una instrucción propia con opcode `0x3F` (fuera de los
opcodes del MIPS) para no chocar con ninguna instrucción real. Se documenta
claramente en el enum.

**Código**: `src/instruccion/Opcode.java:7-9` (comentario) y `:28`;
`src/instruccion/Instruccion.java:93` (`codificarHalt`).

### P22. ¿Qué hace el `syscall` y por qué usa `$v0` y `$a0`?
**R.** Es la llamada al sistema: el servicio se pone en `$v0` y el argumento en
`$a0`. Soporta 1 (entero), 4 (cadena), 5 (leer entero), 11 (carácter) y 10
(terminar). En un procesador real sería la puerta entre modo usuario y SO.

**Código**: `src/cpu/CPU.java:218-266` (`syscall()`), `:219` (lee `$v0`),
`:253-255` (terminar); servicios en `src/instruccion/ServicioSyscall.java:9-13`.

---

## D. ALU, datos y desbordamiento

### P23. ¿Qué operaciones hace la ALU?
**R.** Aritmética (`add`, `addu`, `sub`, `subu`), lógica (`and`, `or`, `xor`,
`nor`), comparación (`slt`, `sltu`), desplazamiento (`sll`, `srl`), `lui` y
`nop`. Dos operandos de 32 bits → 32 bits de resultado.

**Código**: `src/alu/ALU.java:11-53` (todo el `switch`), `src/alu/OperacionALU.java`.

### P24. ¿Qué pasa con el desbordamiento (overflow)?
**R.** `add` y `sub` verifican el resultado en 64 bits y, si no cabe en 32,
lanzan `ArithmeticException("Desbordamiento…")` → la ejecución se detiene con
error. `addu` y `subu` no verifican: envuelven (wrapping). Los programas del
equipo usan `addu`/`addiu` a propósito para que nunca se detengan por eso.

**Código**: `src/alu/ALU.java:13-19` (suma con chequeo), `:20-21` (`addu`),
`:22-28` (resta con chequeo), `:29-30` (`subu`); prueba asociada en
`src/pruebas/PruebasUnitarias.java`.

### P25. ¿Tiene banderas (flags) el procesador?
**R.** No, igual que MIPS: no hay bandera de cero ni de signo. Para comparar se
usa `slt` (deja 1 o 0 en un registro) y luego `beq`/`bne`. Es una de las tres
razones técnicas de la elección.

**Código**: `src/alu/ALU.java:39-42` (`slt`, `sltu`);
`src/ejecucion/EjecutorInstrucciones.java:69-74` (rama usando la comparación).

### P26. ¿Dónde se guarda el resultado de `mult` y `div`?
**R.** En el par de registros internos **HI:LO** (64 bits): HI = resto, LO =
cociente/producto. Se recuperan a un registro con `mfhi` y `mflo`.

**Código**: `src/ejecucion/EjecutorInstrucciones.java:49-60` (cálculo y
`fijarHiLo`), `:61-67` (`mfhi`/`mflo`); `src/cpu/CPU.java:212` (`fijarHiLo`),
`:376-381` (`getHi`/`getLo`).

### P27. ¿Cómo se maneja una división entre cero?
**R.** Se lanza `ArithmeticException("Division por cero")` en el ejecutor: es un
error del programa, no un resultado silencioso.

**Código**: `src/ejecucion/EjecutorInstrucciones.java:51-53`.

---

## E. Programas, ejecución y resultados

### P28. ¿Cómo se construyen los programas sin escribir binario a mano?
**R.** Con `ProgramaBuilder`: se escriben instrucciones tipo `addi`, `lw`,
`etiqueta("bucle")`, `j("bucle")` y al hacer `generar()` se produce el arreglo
de palabras de 32 bits, resolviendo las etiquetas a direcciones.

**Código**: `src/instruccion/ProgramaBuilder.java:131` (`etiqueta`),
los métodos de instrucciones desde `:138`, el llenado de datos en `:92-124`.

### P29. ¿Qué programas hay y qué resultados dan?
**R.** Cinco, todos verificables en vivo:

| Programa | Resultado | Ciclos |
|----------|-----------|--------|
| sumatoria | Sumatoria de 3 a 10 = 52 | 68 |
| factorial | Factorial de 7 = 5040 | 63 |
| integral | Σx² de 1 a 4 = 30 | 56 |
| promedio | Promedio de {4,8,15,16,23} = 13 | 123 |
| perceptron | z = 3, y = 1 | 62 |

**Código**: `src/programas/Sumatoria.java`, `Factorial.java`, `Integral.java`,
`Promedio.java`, `Perceptron.java` (+ `Impresion.java`); se corren con
`./ejecutar_programas.sh`.

### P30. ¿Por qué el promedio tarda 123 ciclos y el integral solo 56?
**R.** Porque hace más trabajo: dos bucles (imprimir el vector y sumarlo) y una
división; el integral hace un solo bucle corto. El contador de ciclos mide
exactamente cuántas instrucciones se ejecutaron.

**Código**: `src/programas/Promedio.java` vs `src/programas/Integral.java`;
contador en `src/cpu/CPU.java:128`.

### P31. ¿Cómo se ejecuta y cómo se ve el estado del procesador?
**R.** `java -cp out ui.Emulador <programa> [--traza] [--estado] [--captura
archivo]`. `--traza` imprime cada ciclo (PC y código hexadecimal de la
instrucción) y `--estado` vuelca registros, PC, HI/LO y el mapa de memoria al
final. También está la ventana gráfica con "Ejecutar 1 ciclo".

**Código**: `src/ui/Emulador.java:43-52` (flags), `:114-124` (bucle y traza),
`:136-141` (`--estado`), `:179-183` (ayuda); ventana en
`src/ui/VentanaEmulador.java`.

### P32. ¿Qué es la ventana gráfica y qué se puede ver en ella?
**R.** Una interfaz Swing (sin librerías externas) que permite ejecutar un ciclo
a la vez (`Ctrl+Intro`) y ver las etapas, los registros, la memoria y las
señales de control de la instrucción actual: es la demo del "interior visible"
del procesador.

**Código**: `src/ui/VentanaEmulador.java`; tema en `src/ui/TemaOscuro.java`;
se abre con `./ventana.sh`.

---

## F. Pruebas y verificación

### P33. ¿Cuántas pruebas hay y qué cubren?
**R.** **201 en total, 0 fallidas**: 117 unitarias (ALU, registros,
decodificador, segmentos, HALT), 44 de integración (ciclo completo, ramas,
saltos, syscall) y 40 de sistema (los 5 programas con su salida exacta).

**Código**: `src/pruebas/PruebasUnitarias.java`, `PruebasIntegracion.java`,
`PruebasSistema.java`; orquestador `src/pruebas/EjecutorPruebas.java:9-24`;
corren con `./probar.sh` y el resumen queda en `salidas/plan_pruebas_completo.txt`.

### P34. ¿Qué framework de pruebas usan?
**R.** Ninguno externo: un mini-framework propio (`MiniTest`) con `assertTrue`,
`assertEquals` y `assertThrows`, por la misma regla de "sin librerías
externas". Devuelve 0 si todo pasa.

**Código**: `src/pruebas/MiniTest.java:19`, `:34`, `:60`, `:100` (`resumen`),
`:113-118` (contadores).

### P35. ¿Qué pruebas demuestran que "la CPU nunca muere"?
**R.** Las que verifican que los programas terminan por HALT con el PC dentro
del mapa, que el PC circular vuelve a 0 y que el límite de 100 000 ciclos
corta los programas sin `halt`.

**Código**: `src/pruebas/PruebasSistema.java:52-67` (cada programa:
"termina", "termina por HALT (PC dentro del mapa)"); 
`src/cpu/CPU.java:136-143` (PC circular) y `:35` (límite).

### P36. ¿Dónde están los planes de pruebas?
**R.** En `docs/pruebas/`: un plan por nivel (unitarias, integración, sistema)
con los casos y su justificación.

**Código**: `docs/pruebas/plan_pruebas_unitarias.md`,
`plan_pruebas_integracion.md`, `plan_pruebas_sistema.md`.

---

## G. Decisiones de diseño y comparación

### P37. ¿Por qué MIPS y no RISC-V o x86?
**R.** RISC-V también era válido y lo comparan en la tabla: MIPS ganó por su
documentación didáctica y por los 3 formatos fijos frente a 6. x86 es CISC con
instrucciones de longitud variable (1 a 15 bytes) y microcódigo: habría ocupado
más el decodizador que el resto de la práctica.

**Código**: evidencia de simplicidad en `src/instruccion/Decodificador.java:45-86`
(toda la decodificación caben en una pantalla).

### P38. ¿Por qué Java y no C o ensamblador?
**R.** Java obliga a pensar en objetos y paquetes, permite modelar la
circuitería con tipos y clases claras, y su manejo de excepciones hace visibles
los errores (desbordamiento, dirección inválida). Además, ensamblador no
serviría: aquí no hay hardware real que ejecutar.

**Código**: estructura de paquetes en `src/` (36 clases, 0 dependencias externas).

### P39. ¿Qué significa "sin librerías externas"?
**R.** Que solo se usan clases de la biblioteca estándar de Java (java.util,
java.io, javax.swing). No hay JUnit, ni Maven, ni nada descargado.

**Código**: `compilar.sh` (compila con `javac` directo, sin gestor de paquetes).

### P40. ¿Es extensible? ¿Cuánto cuesta agregar una instrucción?
**R.** Cuatro archivos y una prueba: `Opcode` (o `Funct`), `Decodificador` (si
necesita extraer campos nuevos), `UnidadControl` (las señales) y
`EjecutorInstrucciones` (si necesita un comportamiento nuevo).

**Código**: `src/instruccion/Opcode.java` → `src/instruccion/Decodificador.java`
→ `src/ejecucion/UnidadControl.java:22` → `src/ejecucion/EjecutorInstrucciones.java:29`.

### P41. ¿Qué diferencia hay entre este emulador y un simulador de eventos?
**R.** Es un **emulador funcional**: reproduce el comportamiento instrucción por
instrucción (semántica y ciclo), no los tiempos eléctricos ni el hardware a
nivel de puertas.

**Código**: `src/cpu/CPU.java:119-130` (la semántica del ciclo).

---

## H. Proceso de trabajo (preguntas incómodas)

### P42. ¿Qué hizo cada integrante?
**R.** Responder con la tabla real del equipo (está en la portada y en
`docs/reporte_exposicion/portada.pdf`). Ejemplo de reparto que cuadra con el
código: uno el núcleo (CPU + ciclo), otro memoria y ALU, otro ISA/programas +
pruebas y demo. No improvisar: acordarlo antes de la exposición.

**Código**: `docs/reporte_exposicion/portada.pdf`; historial de Git:
`git log --stat` (muestra qué archivo tocó quién).

### P43. ¿Usaron inteligencia artificial? ¿Para qué?
**R.** Responder con la verdad y con límites claros: si se usó, fue para
dudas puntuales y revisión, y **la verificación la dio el código**: 201 pruebas
automáticas y 5 programas con salida exacta. Lo que importa es que el
resultado es comprobable, no quién lo escribió.

**Código**: nada que mostrar; mostrar en su lugar `./probar.sh` corriendo.

### P44. ¿Cómo compilan y ejecutan todo?
**R.** Cuatro guiones: `./compilar.sh` (compila), `./probar.sh` (201 pruebas),
`./ejecutar_programas.sh` (los 5 programas), `./ventana.sh` (interfaz gráfica).

**Código**: `compilar.sh`, `probar.sh`, `ejecutar_programas.sh`, `ventana.sh`
en la raíz del proyecto; pasos, salidas reales y guion de la demo en pantalla:
`docs/reporte_exposicion/demo_ejecucion.md`.

### P45. ¿Qué pasa si ejecuto la demo y algo falla?
**R.** Hay respaldos: las capturas de la práctica están en
`docs/reporte_exposicion/reporte_final/imagenes/` y los volcados de texto en
`salidas/`. Se puede mostrar esa evidencia sin perder el hilo.

**Código**: `salidas/plan_pruebas_completo.txt`, `salidas/*_captura.txt`.

---

## Mapa rápido del código

| Qué buscas | Archivo | Líneas clave |
|---|---|---|
| Ciclo y 5 etapas | `src/cpu/CPU.java` | 119 (`ciclo`), 136/146/159/164/175 (etapas), 128 (`ciclos++`) |
| Parada, límite de ciclos, syscall | `src/cpu/CPU.java` | 35 (100000), 218 (`syscall`), 253 (terminar), 273 (`ejecutar`), 330 (`detener`) |
| Contador de programa (PC) | `src/cpu/PC.java` | 32 (`incrementar`), 37 (`reiniciar`) |
| Decodificación de bits | `src/instruccion/Decodificador.java` | 15 (`decodificar`), 45 (opcode), 70 (funct) |
| Opcodes (`0x00`, `0x3F`, lw, sw…) | `src/instruccion/Opcode.java` | 13–28 |
| Operaciones tipo R (funct) | `src/instruccion/Funct.java` | 11–28 |
| NOP y HALT | `src/instruccion/Instruccion.java` | 14 (`PALABRA_NOP`), 93 (`codificarHalt`) |
| Señales de control | `src/ejecucion/UnidadControl.java` | 22 (`decodificar`), 61 (HALT), 70 (tipo R) |
| Aplicar señales (EX) | `src/ejecucion/EjecutorInstrucciones.java` | 29 (`ejecutar`), 31 (halt), 69 (ramas), 99 (ALU) |
| Definición de señales | `src/ejecucion/SenalesControl.java` | 16–56 |
| ALU y desbordamiento | `src/alu/ALU.java` | 11 (`operar`), 16 y 25 (desbordamiento) |
| Registros y `$zero` | `src/memoria/Registros.java` | 29 (`leer`), 34 (`escribir`) |
| Mapa de memoria | `src/memoria/MemoriaSistema.java` | 35 (`segmentoDe`), 62/67 (leer/escribir), 81 (`mapa`) |
| ROM y bloqueo | `src/memoria/ROM.java` | 17 (base 0), 35 (cargar), 50 (bloquear) |
| RAM y disco | `src/memoria/RAM.java`, `DiscoDuro.java` | base 100 / base 200 |
| Direcciones de datos ("punteros") | `src/instruccion/ProgramaBuilder.java` | 92 (`palabra`), 126 (`celda`), 254 (`lw`) |
| Etiquetas de bucles | `src/instruccion/ProgramaBuilder.java` | 131 (`etiqueta`) |
| Programas de usuario | `src/programas/*.java` | 5 archivos + `Impresion.java` |
| Consola, `--traza`, `--estado` | `src/ui/Emulador.java` | 43 (flags), 114 (bucle), 179 (ayuda) |
| Ventana gráfica | `src/ui/VentanaEmulador.java` | — |
| Pruebas | `src/pruebas/*.java` | `EjecutorPruebas.java:9`, `MiniTest.java:19` |
| Documentación técnica | `docs/arquitectura.md`, `docs/instrucciones.md` | índice completo |

## Recetas para encontrar algo sin saber de Java

Desde la raíz del proyecto:

```bash
# ¿Dónde se detiene la CPU?
grep -rn "detener\|detenido" src/

# ¿Dónde está el límite de ciclos?
grep -rn "LIMITE_CICLOS" src/

# ¿Cómo se extraen los bits de una instrucción?
grep -n "public static int" src/instruccion/Decodificador.java

# ¿Qué hace la ALU con la suma?
grep -n "case ADD" src/alu/ALU.java

# ¿Cuántas pruebas hay realmente?
grep -c "\[PASS\]" salidas/plan_pruebas_completo.txt

# Correr todo y ver el resumen
./probar.sh

# Ver los 5 programas con sus ciclos
./ejecutar_programas.sh | grep "ciclos:"
```

## Datos duros que conviene recordar

| Dato | Valor |
|------|-------|
| Archivos Java / clases del núcleo | 36 / 22 |
| Instrucciones implementadas | 33 (18 R + 12 I + 2 J + halt) |
| Memoria | 300 palabras de 32 bits (ROM 0–99, RAM 100–199, disco 200–299) |
| Registros | 32 × 32 bits (`$zero` inmutable) |
| Etapas del ciclo | 5 (IF, ID, EX, MEM, WB), secuenciales |
| Pruebas | 201 (117 + 44 + 40), 0 fallidas |
| Programas | 5 (52/68, 5040/63, 30/56, 13/123, z=3 y=1/62) |
| Límite de seguridad | 100 000 ciclos |
| Dependencias externas | 0 |
